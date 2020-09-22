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
          env.depId = vraDeployFromCatalog(
              configFormat: "yaml",
              config: readFile('infrastructure.yaml'))[0].id
          env.ip = vraWaitForAddress(
              deploymentId: env.depId,
              resourceName: 'UbuntuMachine')[0]
          echo "Deployed: ${env.depId} address: ${env.ip}"
          def sshStuff = vraRunAction(
              deploymentId: env.depId,
              actionId: 'Cloud.Machine.Remote.PrivateKey',
              resourceName: 'UbuntuMachine')
          echo "${sshStuff}"
         }
       }
     }
   }
}
