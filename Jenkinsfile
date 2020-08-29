@Library('vra8@master')_

def vra = new VRA8("https://bogus", "bogus")

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
          withCredentials([usernameColonPassword(credentialsId: 'vRACloudToken', variable: 'vraToken')]) {
            vra.deployFromCatalog('plain-ubuntu-18', '4', 'Pontus Project', 'Test ' + System.currentTimeMillis())
          }
      }
  }
}
