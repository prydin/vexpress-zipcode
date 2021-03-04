pipeline {
    agent any

    parameters {
        string(defaultValue: 'dev', description: 'Target environment', name: 'ENVIRONMENT', trim: true)
    }

    stages {
        stage('Init') {
            steps {
                script {
                    def gradle = readFile(file: 'build.gradle')
                    env.version = (gradle =~ /version\s*=\s*["'](.+)["']/)[0][1]
                    echo "Inferred version: ${env.version}"
                }
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean assemble'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
                junit 'build/test-results/test/*.xml'
            }
        }

        stage('Publish') {
            steps {
                archiveArtifacts(artifacts: "build/libs/zipcode-${env.version}.jar", fingerprint: true, onlyIfSuccessful: true)
                archiveArtifacts(artifacts: 'src/main/sql/initPostgres.sql', fingerprint: true, onlyIfSuccessful: true)
            }
        }

        stage("InitDeployment") {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dbCreds', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                    script {
                        env.DBUSER = USER
                        env.DBPASSWORD = PASSWORD
                    }
                }
                withCredentials([usernamePassword(credentialsId: 'apiToken', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                    script {
                        env.apiUser = USER
                        env.apiToken = PASSWORD
                    }
                }
            }
        }

        stage('DeployVMs') {
            steps {
                parallel(
                        appServer: {
                            withCredentials([usernamePassword(credentialsId: 'sshCreds', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                                script {
                                    def depId = vraDeployFromCatalog(
                                            configFormat: "yaml",
                                            config: readFile('infra/appserver.yaml'))[0].id
                                    vraWaitForAddress(
                                            deploymentId: depId,
                                            resourceName: 'JavaServer')[0]
                                    env.appIp = getInternalAddress(depId, "JavaServer")
                                    echo "Deployed: ${depId} address: ${env.appIp}"
                                    env.appDeploymentId = depId
                                }
                            }
                        },
                        dbServer: {
                            withCredentials([usernamePassword(credentialsId: 'sshCreds', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                                script {
                                    def depId = vraDeployFromCatalog(
                                            configFormat: "yaml",
                                            config: readFile('infra/dbserver.yaml'))[0].id
                                    vraWaitForAddress(
                                            deploymentId: depId,
                                            resourceName: 'DBServer')[0]
                                    env.dbIp = getInternalAddress(depId, "DBServer")
                                    echo "Deployed: ${depId} address: ${env.dbIp}"
                                    env.dbDeploymentId = depId
                                }
                            }
                        })
            }
        }
        stage('Configure') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'sshCreds', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                    script {
                        def remote = [:]
                        remote.name = 'dbServer'
                        remote.host = env.dbIp
                        remote.user = USER
                        remote.password = PASSWORD
                        remote.allowAnyHosts = true

                        // The first first attempt may fail if cloud-init hasn't created user account yet
                        retry(20) {
                            sleep time: 10, unit: 'SECONDS'
                            sshPut remote: remote, from: 'src/main/sql/initPostgres.sql', into: '/tmp'
                        }
                        sshCommand remote: remote, command: "while [ ! -f /tmp/postgres-running ]; do sleep 1; done"
                        sshCommand remote: remote, command: 'echo "create database zipcodes" | sudo -u postgres psql'
                        sshCommand remote: remote, command: "sudo -u postgres psql zipcodes < /tmp/initPostgres.sql"
                        sshCommand remote: remote, command: "rm /tmp/initPostgres.sql"

                        def txt = readFile(file: 'templates/application-properties.tpl')
                        txt = txt.replace('$DBUSER', env.DBUSER).replace('$DBPASSWORD', env.DBPASSWORD).replace('$DBADDRESS', env.dbIp)
                        writeFile(file: "application.properties", text: txt)

                        remote = [:]
                        remote.name = 'appServer'
                        remote.host = env.appIp
                        remote.user = USER
                        remote.password = PASSWORD
                        remote.allowAnyHosts = true

                        // The first first attempt may fail if cloud-init hasn't created user account yet
                        retry(20) {
                            sleep time: 10, unit: 'SECONDS'
                            sshPut remote: remote, from: 'application.properties', into: '/tmp'
                        }
                        sshPut remote: remote, from: 'scripts/vexpress-zipcode.service', into: '/tmp'
                        sshPut remote: remote, from: 'scripts/configureAppserver.sh', into: '/tmp'
                        sshCommand remote: remote, command: 'chmod +x /tmp/configureAppserver.sh'
                        sshCommand remote: remote, sudo: true, command: "/tmp/configureAppserver.sh ${USER} ${env.apiUser} ${env.apiToken} ${env.BUILD_URL} ${env.version}"
                    }
                }
            }
        }
        stage('Finalize') {
            steps {
                // Make sure this runs after both DB and appserver are fully configured
                withCredentials([usernamePassword(credentialsId: 'sshCreds', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                    script {
                        def remote = [:]
                        remote.name = 'appServer'
                        remote.host = env.appIp
                        remote.user = USER
                        remote.password = PASSWORD
                        remote.allowAnyHosts = true
                        sshCommand remote: remote, sudo: true, command: "systemctl start vexpress-zipcode"
                    }
                }
                // Store build state
                withAWS(credentials: 'jenkins') {
                    writeJSON(file: 'state.json', json: ['url': "http://${env.appIp}:8080", 'deploymentIds': [env.appDeploymentId, env.dbDeploymentId]])
                    s3Upload(file: 'state.json', bucket: 'prydin-build-states', path: "vexpress/zipcode/${params.ENVIRONMENT}/state.json")
                }
            }
        }
    }
}

def getInternalAddress(id, resourceName) {
    def dep = vraGetDeployment(
            deploymentId: id,
            expandResources: true
    )
    return dep.resources.find({ it.name == resourceName }).properties.networks[0].address
}

