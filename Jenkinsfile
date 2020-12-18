pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh './gradlew build'
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
                                    env.appIp = vraWaitForAddress(
                                            deploymentId: depId,
                                            resourceName: 'JavaServer')[0]
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
                                    env.dbIp = vraWaitForAddress(
                                            deploymentId: depId,
                                            resourceName: 'DBServer')[0]
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
                                    remote.name = 'test'
                                    remote.host = env.appIp
                                    remote.user = '${USER}'
                                    remote.password = '${PASSWORD}'
                                    remote.allowAnyHosts = true
                                    stage('Remote SSH') {
                                        sshCommand remote: remote, command: "ls -lrt"
                                        sshCommand remote: remote, command: "for i in {1..5}; do echo -n \"Loop \$i \"; date ; sleep 1; done"
                                    }
                                }
                            }
                        }
                )
            }
        }
    }
}

