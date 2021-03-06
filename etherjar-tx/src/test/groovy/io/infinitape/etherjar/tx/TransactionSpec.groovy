/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.infinitape.etherjar.tx

import io.infinitape.etherjar.domain.Address
import io.infinitape.etherjar.domain.Wei
import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class TransactionSpec extends Specification {

    def "read transaction 0x19442f"() {
        setup:
        def tx = Hex.decodeHex("f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b")
        when:
        def act = Transaction.fromRlp(tx)
        act.signature.message = act.hash(false)

        then:
        act.nonce == 15524
        act.gasPrice.toString(16) == "59b9b95f0"
        act.gas == 0x03d090
        act.to.toHex() == "0x8b3b3b624c3c0397d3da8fd861512393d51dcbac"
        act.value.toHex() == "0x0"
        act.data.toHex() == "0x667a2f58"
        act.signature != null
        act.signature.v == 28
        act.signature.r.toString(16) == "d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64"
        act.signature.s.toString(16) == "39837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b"

        act.signature.recoverAddress().toHex() == "0xed059bc543141c8c93031d545079b3da0233b27f"
    }

    def "Unsigned hash"() {
        setup:
        def tx = Transaction.fromRlp(
                Hex.decodeHex("f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b")
        )
        when:
        def hash = tx.hash(false)
        then:
        Hex.encodeHexString(hash) == "383caae49692ae021fb2189933518ca58fd04d88e99b41a4d18f5ae5fb5f52aa"
    }

    def "Signed hash"() {
        setup:
        def tx = Transaction.fromRlp(
                Hex.decodeHex("f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b")
        )
        when:
        def hash = tx.hash(true)
        then:
        Hex.encodeHexString(hash) == "19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31"
    }

    def "Parse goerli 0x6d4d85"() {
        // 0x6d4d85482c59b6fe2f416996c802ceae2e30b9fe6bc27fe5c72d2fa9b1b2e28b
        setup:
        def tx = Hex.decodeHex("f8cb82afdc843b9aca008303d090947ef66b77759e12caf3ddb3e4aff524e577c59d8d80b864e9c6c1760000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000043c636a65ea4943acfacb227680b6ba20c477ba24ef87049a4a5b3958385e215bb08641ba01e8a3bacc31fc91ade73278d0267b70d38b53623ab0a28d1e20e133286f8a85ca02f2e0ac2c4e9e4410804fa62c9cba70e18eed8b93ec2a2ad5762279de8288b63")
        when:
        def act = Transaction.fromRlp(tx)
        act.signature.message = act.hash(false)

        then:
        act.nonce == 45020
        act.gasPrice == Wei.ofUnits(1, Wei.Unit.GWEI).amount
        act.gas == 250000
        act.to.toHex() == "0x7ef66b77759e12caf3ddb3e4aff524e577c59d8d"
        act.value.toHex() == "0x0"
        act.data.toHex() == "0xe9c6c1760000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000043c636a65ea4943acfacb227680b6ba20c477ba24ef87049a4a5b3958385e215bb0864"
        act.signature != null

        act.signature.recoverAddress() == Address.from("0x79047abf3af2a1061b108d71d6dc7bdb06474790")
    }

    def "Parse goerli 0x8d8367"() {
        // 0x8d8367acc4c8f17fa6b9e8a856833b3406b200f96a5fa98018128411b1f0c6d1
        setup:
        def tx = Hex.decodeHex("f86c8227b2843b9aca008275309413ac1a2c6d1a4efc492a40d8f9d4e9f14b7c726887b1a2bc2ec50000001ca077313351aaa29a277e3cf015c354542e042b00c4757e1ac70fdbc9b1d0341c23a079c62b0c278676c590afd0f8bcfc4654b5babb99a5883baefc539acd55ee0365")
        when:
        def act = Transaction.fromRlp(tx)
        act.signature.message = act.hash(false)

        then:
        act.nonce == 10162
        act.gasPrice == Wei.ofUnits(1, Wei.Unit.GWEI).amount
        act.gas == 30000
        act.to.toHex() == "0x13ac1a2c6d1a4efc492a40d8f9d4e9f14b7c7268"
        act.value == Wei.ofEthers(0.05)
        act.data.toHex() == "0x00"
        act.signature != null
        act.signature.v == 28
        act.signature.r.toString(16) == "77313351aaa29a277e3cf015c354542e042b00c4757e1ac70fdbc9b1d0341c23"
        act.signature.s.toString(16) == "79c62b0c278676c590afd0f8bcfc4654b5babb99a5883baefc539acd55ee0365"

        act.signature.recoverAddress().toHex() == "0x8ced5ad0d8da4ec211c17355ed3dbfec4cf0e5b9"
    }

    def "Parse morden tx"() {
        setup:
        def tx = Hex.decodeHex("f86d808501faa3b500825208945b30de96fdf94ac6c5b4a8c243f991c649d66fa188a688906bd8b0000080819fa008f248f8a0b9353c4261dc88bf879e9f76d72d92cf20b50a38af0074295868cba067c01e7c676df52cec7b4abe50b1467c83709c8d2e78f92529e0a41bf22557a0")
        when:
        def act = Transaction.fromRlp(tx)
        act.signature.message = act.hash(false)

        then:
        act.nonce == 0
        act.gasPrice == 8500000000
        act.gas == 21000
        act.to.toHex() == "0x5b30de96fdf94ac6c5b4a8c243f991c649d66fa1"
        act.value == Wei.ofEthers(12)
        act.data.toHex() == "0x"
        act.signature != null
        act.signature instanceof SignatureEip155
        act.signature.v == 159

        act.signature.recoverAddress().toHex() == "0x7beba82e2f24ec647b916b9cbd928fb06cf0c71b"
    }
}
