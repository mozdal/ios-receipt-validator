# ios-receipt-validator
iOS Receipt Validator - Spring Boot

## How it works

This spring boot application gets itunes subscription sent from the iOS device and sends it to iTunes server with your secret. The response from the iTunes verification service is then returned to the iOS device for further verification operations.

## Sample swift verification method

A sample method to make a request to receipt validator can be found here. Be careful not to Base64 encode the payload both in client and the server. The sample makes encoding client side on iOS device.

https://gist.github.com/mozdal/7b2a15556fe5c0f37f0d3fbbcfe43e90

Please contribute :)
