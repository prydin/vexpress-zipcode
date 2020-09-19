pipeline {
  agent any
  
  parameters {
    string("ip")
    string("depId")
    string("sshkey")
  }
  
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
    
    stage('Deploy') {
      steps {
        env.depId = vraDeployFromCatalog(
            configFormat: "yaml",
            config: readFile('infrastructure.yaml'))[0].id
        env.ip = vraWaitForAddress(
            deploymentId: env.depId,
            resourceName: 'UbuntuMachine')
        echo "Deployed: ${env.depId} address: ${env.ip}"
        }
     }
   }
 }
}
