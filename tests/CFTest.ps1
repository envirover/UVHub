$stackName  = "test"

$bucketName = "envirover"
$keyPrefix  = "test"
$uvhubgz    = "uvhub-2.0-bin.tar.gz"
$uvtracksgz = "uvtracks-2.0-bin.tar.gz"

Remove-CFNStack -StackName $stackName -Region us-west-2 -Force 

cd C:\envirover\SPL\UVHub
mvn clean install

Write-S3Object -BucketName $bucketName -Key $keyPrefix/$uvtracksgz -File C:\envirover\SPL\UVHub\distribution\target\$uvtracksgz
Set-S3ACL -BucketName $bucketName -Key $keyPrefix/$uvtracksgz -PublicReadOnly

Write-S3Object -BucketName $bucketName -Key $keyPrefix/$uvhubgz -File C:\envirover\SPL\UVHub\distribution\target\$uvhubgz
Set-S3ACL -BucketName $bucketName -Key $keyPrefix/$uvhubgz -PublicReadOnly

Write-S3Object -BucketName $bucketName -Key $keyPrefix/uvhub.template -File C:\envirover\SPL\UVHub\distribution\aws\uvhub.template
Set-S3ACL -BucketName $bucketName -Key $keyPrefix/uvhub.template -PublicReadOnly

New-CFNStack -StackName $stackName -DisableRollback $true -TemplateURL https://s3-us-west-2.amazonaws.com/$bucketName/$keyPrefix/uvhub.template `
             -Capability "CAPABILITY_IAM" -Region us-west-2 `
             -Parameter @(@{ParameterKey="ClientCIDR"; ParameterValue="0.0.0.0/0"}, 
                          @{ParameterKey="KeyName"; ParameterValue="spl"}, 
                          @{ParameterKey="InstanceType"; ParameterValue="t3.micro"},
                          @{ParameterKey="RockBlockIMEI"; ParameterValue="300434063821990"}, 
                          @{ParameterKey="RockBlockUsername"; ParameterValue="pb@envirover.com"}, 
                          @{ParameterKey="RockBlockPassword"; ParameterValue="gis.lord32"},
                          @{ParameterKey="MAVAutopilot"; ParameterValue="ArduPilot"},
                          @{ParameterKey="MAVType"; ParameterValue="QUADROTOR"},
                          @{ParameterKey="UVHubArchiveURL"; ParameterValue="https://s3-us-west-2.amazonaws.com/$bucketName/$keyPrefix/$uvhubgz"},
                          @{ParameterKey="UVTracksArchiveURL"; ParameterValue="https://s3-us-west-2.amazonaws.com/$bucketName/$keyPrefix/$uvtracksgz"} )