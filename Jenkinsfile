pipeline {
    agent any

    stages {
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
                archiveArtifacts(artifacts: 'build/libs/zipcode-*.jar ', fingerprint: true, onlyIfSuccessful: true)
                archiveArtifacts(artifacts: 'src/main/sql/initPostgres.sql ', fingerprint: true, onlyIfSuccessful: true)
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
                                    echo "Deployed: ${depId} external address: ${env.dbIp}, internal address: ${internalIp}"
                                }
                            }
                        })
            }
        }
        stage('Configure') {
            steps {
                parallel(
                        dbServer: {
                            withCredentials([usernamePassword(credentialsId: 'sshCreds', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                                script {
                                    def remote = [:]
                                    remote.name = 'test'
                                    remote.host = env.appIp
                                    remote.user = USER
                                    remote.password = PASSWORD
                                    remote.allowAnyHosts = true
                                    echo "Remote: $remote"
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

