package main

import (
	"configs"
	"crypto/rsa"
	"crypto/x509"
	"encoding/pem"
	"flag"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"os/user"
	"path/filepath"
	"strconv"
	"time"

	"github.com/dgrijalva/jwt-go"
)

var (
	tokenLifetime = flag.Int("lifetime", 300, "seconds before tokens expire")
	keyPath       = flag.String("key-path", "", "path to private key")
	keyPassword   = flag.String("key-pass", "", "password used to open private key")
	username      = flag.String("username", "", "username to place in the token")
	email         = flag.String("email", "", "email address to place in the token")
	givenName     = flag.String("given-name", "", "given name to place in the token")
	familyName    = flag.String("family-name", "", "family name to place in the token")
	name          = flag.String("name", "", "name to place in the token")
)

func init() {
	flag.Parse()
}

func homeParameterFile() string {
	usr, err := user.Current()
	if err != nil {
		log.Fatal(err)
	}

	return filepath.Join(usr.HomeDir, ".make-jwt")
}

func loadParameterFile(path string) {

	// Open the parameter file.
	paramFile, err := configs.NewParamFile(path)
	if os.IsNotExist(err) {
		return
	}
	if err != nil {
		log.Println(err)
		return
	}
	defer paramFile.Close()

	// Load individual parameters from the file.
	for paramFile.NextParam() {
		paramName, paramValue := paramFile.ExtractParam()
		if *paramName == "lifetime" {
			lifetime, err := strconv.Atoi(*paramValue)
			if err != nil {
				log.Printf("Invalid token lifetime: %s\n", *paramValue)
			} else {
				tokenLifetime = &lifetime
			}
		} else if *paramName == "key-path" {
			keyPath = paramValue
		} else if *paramName == "key-pass" {
			keyPassword = paramValue
		} else if *paramName == "username" {
			username = paramValue
		} else if *paramName == "email" {
			email = paramValue
		} else if *paramName == "given-name" {
			givenName = paramValue
		} else if *paramName == "family-name" {
			familyName = paramValue
		} else if *paramName == "name" {
			name = paramValue
		} else {
			log.Printf("unrecognized parameter name: %s\n", *paramName)
		}
	}
}

func loadParameterFiles() {
	loadParameterFile(homeParameterFile())
	loadParameterFile(".make-jwt")
}

func loadSigningKey() (*rsa.PrivateKey, error) {

	// Verify that we have a key path.
	if keyPath == nil || *keyPath == "" {
		return nil, fmt.Errorf("missing required parameter: key-path")
	}

	// Read the key file.
	pemData, err := ioutil.ReadFile(*keyPath)
	if err != nil {
		return nil, fmt.Errorf("unable to read private key file: %s", err)
	}

	// Extract the PEM-encoded data block.
	block, _ := pem.Decode(pemData)
	if block == nil {
		return nil, fmt.Errorf("bad private key data: not PEM-encoded")
	}
	if got, want := block.Type, "RSA PRIVATE KEY"; got != want {
		return nil, fmt.Errorf("unknown key type %q: expected %q", got, want)
	}

	// Get the key bytes.
	keyBytes := []byte(nil)
	if x509.IsEncryptedPEMBlock(block) {

		// We need the password if the key is encrypted.
		if keyPassword == nil || *keyPassword == "" {
			return nil, fmt.Errorf("no password provided for private key")
		}

		// Decrypt the data block.
		keyBytes, err = x509.DecryptPEMBlock(block, []byte(*keyPassword))
		if err != nil {
			return nil, fmt.Errorf("unable to decrypt private key: %s", err)
		}
	} else {
		keyBytes = block.Bytes
	}

	// Decode the private key.
	privateKey, err := x509.ParsePKCS1PrivateKey(keyBytes)
	if err != nil {
		return nil, fmt.Errorf("bad private key: %s", err)
	}

	return privateKey, nil
}

func generateToken(key *rsa.PrivateKey) (string, error) {
	token := jwt.New(jwt.SigningMethodRS256)

	// Add the claims to the token.
	if username != nil && *username != "" {
		token.Claims["sub"] = *username
	}
	if email != nil && *email != "" {
		token.Claims["email"] = *email
	}
	if givenName != nil && *givenName != "" {
		token.Claims["given_name"] = *givenName
	}
	if familyName != nil && *familyName != "" {
		token.Claims["family_name"] = *familyName
	}
	if name != nil && *name != "" {
		token.Claims["name"] = *name
	}

	// Set the token expiration time.
	duration := time.Duration(*tokenLifetime) * time.Second
	token.Claims["exp"] = time.Now().Add(duration).Unix()

	// Sign and encode the token.
	return token.SignedString(key)
}

func main() {
	loadParameterFiles()

	// Load the signing key.
	signingKey, err := loadSigningKey()
	if err != nil {
		log.Fatal(err)
	}

	// Generate and print the token.
	token, err := generateToken(signingKey)
	if err != nil {
		log.Fatal(err)
	}
	fmt.Print(token)
}
