# SmiloJar

[![Build Status](https://travis-ci.org/Smilo-platform/smilojar.svg?branch=master)](https://travis-ci.org/Smilo-platform/smilojar)
[![codecov](https://codecov.io/gh/Smilo-platform/smilojar/branch/master/graph/badge.svg)](https://codecov.io/gh/Smilo-platform/smilojar)
[![license](https://img.shields.io/github/license/Smilo-platform/smilojar.svg?maxAge=2592000)](https://github.com/Smilo-platform/smilojar/blob/master/LICENSE)

Framework agnostic modular Java 8+ integration library for [Smilo blockchains](https://smilo.io)

Latest Version: 0.6.0

## Architecture

* [ ] High-level [web3.js](https://github.com/Smilo-platform/go-smilo/wiki/JavaScript-API)-like Java 8 API (_in progress_)
* [x] Low-level [JSON-RPC API](https://github.com/Smilo-platform/go-smilo/wiki/JSON-RPC)
* [x] Transport data-layer 
  * [ ] IPC (_not implemented yet_)  
  * [x] HTTP
  * [ ] WebSockets (_in progress_)

## Modules

Structure of dependencies between modules:

* `etherjar-abi`
  * `etherjar-domain`
  * `etherjar-hex`
* `etherjar-domain`
* `etherjar-hex`  
* `etherjar-rlp`
* `etherjar-rpc-api`  
  * `etherjar-domain`
  * `etherjar-hex`  
* `etherjar-rpc-http`
  * `etherjar-rpc-api`
  * `etherjar-domain`
  * `etherjar-hex`
* `etherjar-solidity`
  * `etherjar-abi`
  * `etherjar-domain`
* `etherjar-tx`
  * `etherjar-rlp`
  * `etherjar-domain`

where

* `etherjar-abi` - Smart contract [Application Binary Interface (ABI)](https://github.com/Smilo-platform/go-smilo/wiki/Ethereum-Contract-ABI)
* `etherjar-domain` - Core module contains pure domain logic (`Address`, `Block`, `Transaction`, `Wei` and so on)
* `etherjar-hex` - Hexadecimal encoding and encoding utils for `String`, `BigInteger`, byte arrays
* `etherjar-rlp` - Reading and writing RLP (Recursive Length Prefix) encoded data 
* `etherjar-rpc-api` - [JSON-RPC API](https://github.com/Smilo-platform/go-smilo/wiki/JSON-RPC) generic implementation
* `etherjar-rpc-http` - HTTP transport implementation for JSON-RPC API data-layer
* `etherjar-solidity` - Thin wrapper around [`solc` Solidity compiler](https://github.com/ethereum/solidity)
* `etherjar-tx` - Read, verify and manipulate Transactions   

## Usage

### Maven

```xml
<dependency>
  <groupId>io.smilo</groupId>
  <artifactId>etherjar-rpc-http</artifactId>
  <version>0.5.0</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/Smilo-platform/smilojar" 
    }
}

dependencies {
    compile 'io.smilo:etherjar-rpc-http:0.6.0'
}
```

## Examples

How to call `web3_clientVersion` low-level JSON-RPC API method:

```java
package example;

import io.smilo.smilojar.rpc.transport.DefaultRpcTransport;
import io.smilo.smilojar.rpc.transport.RpcTransport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args)
            throws URISyntaxException, IOException, ExecutionException, InterruptedException {

        try (RpcTransport trans =
                     new DefaultRpcTransport(new URI("http://127.0.0.1:8545"))) {

            Future<String> req =
                    trans.execute("web3_clientVersion", Collections.EMPTY_LIST, String.class);

            System.out.println(String.format("Client version: %s", req.get()));
        }
    }
}
```

## Documentation

* [Reference Guide](./docs/index.md)

## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/Smilo-platform/smilojar/issues).

## Licence

[Apache 2.0](LICENSE)
