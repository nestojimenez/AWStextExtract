//snippet-sourcedescription:[Demonstrations of the AWS SDK for Go V2 SDK features]
//snippet-keyword:[go,config]
//snippet-sourcetype:[full-example]
//snippet-sourcedate:[10/27/2021]
//snippet-sourceauthor:[gangwere]
package sdk

import (
	"context"
	"fmt"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/credentials/stscreds"
	"github.com/aws/aws-sdk-go-v2/service/sts"
)

func main() {

	//snippet-start:[go.sdk.LoadDefaultConfig]
	cfg, err := config.LoadDefaultConfig(context.Background())
	if err != nil {
		panic("Couldn't load config!")
	}
	fmt.Println("The loaded default region is:" + cfg.Region)

	//snippet-end:[go.sdk.LoadDefaultConfig]

	//snippet-start:[go.sdk.AssumeRoleConfig]

	// To acquire credentials for temporary use, STS should be used.
	// An AssumeRoleProvider can be used to retrieve the appropriate credentials as needed.
	stsSvc := sts.NewFromConfig(cfg)
	creds := stscreds.NewAssumeRoleProvider(stsSvc, "myRoleArn")
	// If you want, you can overwrite the current credentials and pivot to the assumed role
	cfg.Credentials = aws.NewCredentialsCache(creds)
	// If you wish to continue using the original credentials alongside the assumed credentials,
	// copy the configuration into a new variable.
	cloneCfg := cfg.Copy()
	cloneCfg.Credentials = aws.NewCredentialsCache(creds)

	//snippet-end:[go.sdk.AssumeRoleConfig]

}
