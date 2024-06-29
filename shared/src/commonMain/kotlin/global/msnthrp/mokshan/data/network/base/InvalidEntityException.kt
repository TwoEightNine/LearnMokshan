package global.msnthrp.mokshan.data.network.base

class InvalidEntityException(
    entityName: String,
    url: String,
) : IllegalStateException("Invalid $entityName received from $url")