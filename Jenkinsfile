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
                withCredentials([usernamePassword(credentialsId: 'sshCreds', passwordVariable: 'PASSWORD', usernameVariable: 'USER')]) {
                    parallel(
                            appServer: {
                                script {
                                    def depId = vraDeployFromCatalog(
                                            configFormat: "yaml",
                                            config: readFile('infra/appserver.yaml'))[0].id
                                    env.appIp = vraWaitForAddress(
                                            deploymentId: depId,
                                            resourceName: 'JavaServer')[0]
                                    echo "Deployed: ${depId} address: ${env.appIp}"
                                }
                            },
                            dbServer: {
                                script {
                                    def depId = vraDeployFromCatalog(
                                            configFormat: "yaml",
                                            config: readFile('infra/dbserver.yaml'))[0].id
                                    env.dbIp = vraWaitForAddress(
                                            deploymentId: depId,
                                            resourceName: 'DBServer')[0]
                                    echo "Deployed: ${depId} address: ${env.dbIp}"
                                }
                            })
                }
            }
        }
    }
}

