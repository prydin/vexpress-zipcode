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
            catalogItemName: 'plain-ubuntu-18', 
            count: 1, 
            deploymentName: 'Jenkins-#', 
            projectName: 'Pontus Project', 
            reason: 'Test', 
            timeout: 300, 
            version: '6',
            inputs: '{ username: \'testuser\' }')
          assert dep != null
          def addr = vraWaitForAddress(dep[0].id)
          echo "Deployed: $dep[0].id, address: $addr"
        }
     }
   }
 }
}
