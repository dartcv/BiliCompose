package top.sacz.bili.api

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable


sealed class BiliResponse<out T> {
    data object Loading : BiliResponse<Nothing>()
    data object Wait : BiliResponse<Nothing>()

    @Serializable
    data class Success<T>(
        val code: Int,
        val message: String,
        val ttl: Int,
        val data: T
    ) : BiliResponse<T>()

    @Serializable
    data class SuccessOrNull<T>(
        val code: Int,
        val message: String,
        val ttl: Int,
        val data: T?
    ) : BiliResponse<T>()

    data class Error(
        val code: Int, val msg: String, val cause: Throwable = ApiException(
            code, msg,
            cause = Throwable()
        )
    ) : BiliResponse<Nothing>()
}

fun <T> BiliResponse<T>.isSuccess(): Boolean = when(this) {
    is BiliResponse.Success -> true
    is BiliResponse.SuccessOrNull -> true
    else -> false
}

fun <T> BiliResponse<T>.isLoading(): Boolean = this is BiliResponse.Loading
fun <T> BiliResponse<T>.isWait(): Boolean = this is BiliResponse.Wait
fun <T> BiliResponse<T>.isError(): Boolean = this is BiliResponse.Error


/**
 * DSL构建器风格的状态监听器
 */
@Composable
fun <T> BiliResponse<T>.registerStatusListener(block: StatusListenerBuilder<T>.() -> Unit) {
    val builder = StatusListenerBuilder<T>().apply(block)
    when (this) {
        is BiliResponse.Loading -> builder.onLoading?.invoke()
        is BiliResponse.Wait -> builder.onWait?.invoke()
        is BiliResponse.Success -> builder.onSuccess?.invoke(data)
        is BiliResponse.SuccessOrNull -> builder.onSuccessOrNull?.invoke(data)
        is BiliResponse.Error -> builder.onError?.invoke(code, msg, cause)
    }
}

class StatusListenerBuilder<T> {
    var onLoading: (@Composable () -> Unit)? = null
    var onWait: (@Composable () -> Unit)? = null
    var onSuccess: (@Composable (data: T) -> Unit)? = null
    var onSuccessOrNull: (@Composable (data: T?) -> Unit)? = null
    var onError: (@Composable (code: Int, msg: String, cause: Throwable) -> Unit)? = null
    fun onLoading(block: @Composable () -> Unit) {
        onLoading = block
    }

    fun onWait(block: @Composable () -> Unit) {
        onWait = block
    }

    fun onSuccess(block: @Composable (data: T) -> Unit) {
        onSuccess = block
    }

    fun onSuccessOrNull(block: @Composable (data: T?) -> Unit) {
        onSuccessOrNull = block
    }

    fun onError(block: @Composable (code: Int, msg: String, cause: Throwable) -> Unit) {
        onError = block
    }
}
