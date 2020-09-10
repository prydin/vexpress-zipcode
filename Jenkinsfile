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
    
    stage('Deploy') {
      steps {
        script {
          def dep = vraDeployFromCatalog(
            configFormat: "yaml",
            config: readFile('infrastructure.yaml'))
          assert dep != null
          def addr = vraWaitForAddress(
            deploymentId: dep[0].id,
            resourceName: 'UbuntuMachine')
          echo "Deployed: $dep[0].id, address: $addr"
        }
     }
   }
 }
}
