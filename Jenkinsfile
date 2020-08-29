@Library('vra8@master')_

def vmIp 
def vra
withCredentials([string(credentialsId: 'vRACloudToken', variable: 'vraToken')]) {
  vra = new VRA8("https://api.mgmt.cloud.vmware.com", "$vraToken")
}

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
          def dep = vra.deployFromCatalog('plain-ubuntu-18', '4', 'Pontus Project', 'Invoked from Jenkins ' + System.currentTimeMillis())
          vmIp = vra.waitForIPAddress(dep.id)
        }
        echo "Address of machine is: $vmIp"
     }
   }
 }
}
