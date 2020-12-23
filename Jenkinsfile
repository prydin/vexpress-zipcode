pipeline {
    agent any

    stages {
        stage('Init') {
            steps {
                script {
                    def r = /version\s*=\s*["'](.+)["']/
                    def gradle = readFile(file: 'build.gradle')
                    env.version = (gradle =~ /version\s*=\s*["'](.+)["']/)[0][1]
                    echo "Inferred version: ${env.version}"
                }
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean build'
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
                        def txt = readFile(file: 'templates/application-properties.tpl')
                        txt = txt.replace("$DBUSER", env.DBUSER).replace("$DBPASSWORD", env.DBPASSWORD)
                        writeFile(file: "application.properties", text: txt)
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
                                }
                            }
                        })
            }
        }
        stage('Configure') {
            steps {
                parallel(
                        appServer: {
                            withCredentials([usernamePassword(credentialsId: 'sshCreds', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                                script {
                                    def remote = [:]
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
                                    sshCommand remote: remote, sudo: true, command: "cd /opt\n" +
                                            "mkdir vexpress-zipcode\n" +
                                            "chown ${USER} vexpress-zipcode\n" +
                                            "cd vexpress-zipcode;" +
                                            "wget --auth-no-challenge --user=${env.apiUser} --password=${env.apiToken} ${env.BUILD_URL}/artifact/build/libs/zipcode-${env.version}.jar"
                                }
                            }
                        },
                        dbServer: {
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
                                    sshCommand remote: remote, command: "sudo -u postgres psql < /tmp/initPostgres.sql"
                                    sshCommand remote: remote, command: "rm /tmp/initPostgres.sql"
                                }
                            }
                        }
                )
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

