Remove-CFNStack -StackName test -Force 

cd C:\envirover\SPL\UVHub
mvn clean install

Write-S3Object -BucketName envirover -Key test/uvtracks-2.0-bin.tar.gz -File C:\envirover\SPL\UVHub\distribution\target\uvtracks-2.0-bin.tar.gz
Set-S3ACL -BucketName envirover -Key test/uvtracks-2.0-bin.tar.gz -PublicReadOnly

Write-S3Object -BucketName envirover -Key test/uvhub-2.0-bin.tar.gz -File C:\envirover\SPL\UVHub\distribution\target\uvhub-2.0-bin.tar.gz
Set-S3ACL -BucketName envirover -Key test/uvhub-2.0-bin.tar.gz -PublicReadOnly

Write-S3Object -BucketName envirover -Key test/uvhub.template -File C:\envirover\SPL\UVHub\distribution\aws\uvhub.template
Set-S3ACL -BucketName envirover -Key test/uvhub.template -PublicReadOnly

New-CFNStack -StackName test -DisableRollback $true -TemplateURL https://s3-us-west-2.amazonaws.com/envirover/test/uvhub.template `
             -Capability "CAPABILITY_IAM" -Region us-west-2 `
             -Parameter @(@{ParameterKey="ClientCIDR"; ParameterValue="0.0.0.0/0"}, 
                          @{ParameterKey="KeyName"; ParameterValue="spl"}, 
                          @{ParameterKey="InstanceType"; ParameterValue="t3.micro"},
                          @{ParameterKey="RockBlockIMEI"; ParameterValue="300434063821990"}, 
                          @{ParameterKey="RockBlockUsername"; ParameterValue="pb@envirover.com"}, 
                          @{ParameterKey="RockBlockPassword"; ParameterValue="gis.lord32"},
                          @{ParameterKey="MAVAutopilot"; ParameterValue="ArduPilot"},
                          @{ParameterKey="MAVType"; ParameterValue="QUADROTOR"},
                          @{ParameterKey="UVHubArchiveURL"; ParameterValue="https://s3-us-west-2.amazonaws.com/envirover/test/uvhub-2.0-bin.tar.gz"},
                          @{ParameterKey="UVTracksArchiveURL"; ParameterValue="https://s3-us-west-2.amazonaws.com/envirover/test/uvtracks-2.0-bin.tar.gz"} )