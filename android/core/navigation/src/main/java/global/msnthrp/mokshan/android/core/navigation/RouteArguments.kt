package global.msnthrp.mokshan.android.core.navigation

import java.net.URLEncoder

/**
 * Percent-encodes a value so it can be put into a route such as `screen?title=<value>&id=1`.
 * Spaces become `%20` (not `+`): the navigation library decodes route arguments with
 * `Uri.decode`, which does not turn `+` back into a space.
 */
fun encodeRouteArgument(value: String): String {
    return URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
