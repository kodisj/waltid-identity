package id.walt.webwallet.service.account

/*object Web3WalletAccountStrategy : PasswordlessAccountStrategy<AddressAccountRequest>() {

    override suspend fun register(tenant: String, request: AddressAccountRequest): Result<RegistrationResult> = runCatching {
        val name = request.name

        require(!AccountsService.hasAccountWeb3WalletAddress(request.address)) { "Account already exists with address: ${request.address}" }

        val createdAccountId = transaction {
            val accountId = Accounts.insert {
                it[Accounts.tenant] = tenant
                it[Accounts.name] = name
                it[createdOn] = Clock.System.now().toJavaInstant()
            }[Accounts.id]

            Web3Wallets.insert {
                it[this.tenant] = tenant
                it[Web3Wallets.accountId] = accountId
                it[address] = request.address
                it[ecosystem] = request.ecosystem
                it[owner] = false
            }

            accountId
        }

        return Result.success(RegistrationResult(createdAccountId))
    }

    override suspend fun authenticate(tenant: String, request: AddressAccountRequest): AuthenticatedUser {
        val registeredUserId = if (AccountsService.hasAccountWeb3WalletAddress(request.address)) {
            AccountsService.getAccountByWeb3WalletAddress(request.address).first().id
        } else {
            AccountsService.register(tenant, request).getOrThrow().id
        }
        // TODO: change id to wallet-id (also in the frontend)
        return AddressAuthenticatedUser(registeredUserId, request.address)
    }
}*/
