package nl.knaw.huygens.alexandria.client;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.ImmutableMap;

public class RestRequester<T> {
  private Supplier<Response> responseSupplier;
  private Function<Response, RestResult<T>> mapToFailure = (response) -> RestResult.failingResult(response);
  private Function<Response, RestResult<T>> onOkMapper = mapToFailure;
  private Function<Response, RestResult<T>> onCreatedMapper = mapToFailure;
  private Function<Response, RestResult<T>> onAcceptedMapper = mapToFailure;
  private Function<Response, RestResult<T>> onBadGatewayMapper = mapToFailure;
  private Function<Response, RestResult<T>> onBadRequestMapper = mapToFailure;
  private Function<Response, RestResult<T>> onConflictMapper = mapToFailure;
  private Function<Response, RestResult<T>> onExpectationFailedMapper = mapToFailure;
  private Function<Response, RestResult<T>> onForbiddenMapper = mapToFailure;
  private Function<Response, RestResult<T>> onFoundMapper = mapToFailure;
  private Function<Response, RestResult<T>> onGatewayTimeoutMapper = mapToFailure;
  private Function<Response, RestResult<T>> onGoneMapper = mapToFailure;
  private Function<Response, RestResult<T>> onHttpVersionNotSupportedMapper = mapToFailure;
  private Function<Response, RestResult<T>> onInternalServerErrorMapper = mapToFailure;
  private Function<Response, RestResult<T>> onLengthRequiredMapper = mapToFailure;
  private Function<Response, RestResult<T>> onMethodNotAllowedMapper = mapToFailure;
  private Function<Response, RestResult<T>> onMovedPermanentlyMapper = mapToFailure;
  private Function<Response, RestResult<T>> onNoContentMapper = mapToFailure;
  private Function<Response, RestResult<T>> onNotAcceptableMapper = mapToFailure;
  private Function<Response, RestResult<T>> onNotFoundMapper = mapToFailure;
  private Function<Response, RestResult<T>> onNotImplementedMapper = mapToFailure;
  private Function<Response, RestResult<T>> onNotModifiedMapper = mapToFailure;
  private Function<Response, RestResult<T>> onPartialContentMapper = mapToFailure;
  private Function<Response, RestResult<T>> onPaymentRequiredMapper = mapToFailure;
  private Function<Response, RestResult<T>> onPredonditionFailedMapper = mapToFailure;
  private Function<Response, RestResult<T>> onProxyAuthenticationRequiredMapper = mapToFailure;
  private Function<Response, RestResult<T>> onRequestEntityTooLargeMapper = mapToFailure;
  private Function<Response, RestResult<T>> onRequestTimeoutMapper = mapToFailure;
  private Function<Response, RestResult<T>> onRequestUriTooLongMapper = mapToFailure;
  private Function<Response, RestResult<T>> onRequestedRangeNotSatisfiableMapper = mapToFailure;
  private Function<Response, RestResult<T>> onResetContentMapper = mapToFailure;
  private Function<Response, RestResult<T>> onSeeOtherMapper = mapToFailure;
  private Function<Response, RestResult<T>> onServiceUnavailableMapper = mapToFailure;
  private Function<Response, RestResult<T>> onTemporaryRedirectMapper = mapToFailure;
  private Function<Response, RestResult<T>> onUnauthorizedMapper = mapToFailure;
  private Function<Response, RestResult<T>> onUnsupportedMediaTypeMapper = mapToFailure;
  private Function<Response, RestResult<T>> onUseProxyMapper = mapToFailure;

  public static <T extends Object> RestRequester<T> withResponseSupplier(Supplier<Response> responseSupplier) {
    RestRequester<T> requester = new RestRequester<>();
    requester.responseSupplier = responseSupplier;
    return requester;
  }

  /** Set handler for 200 OK **/
  public RestRequester<T> onOK(Function<Response, RestResult<T>> onOkMapper) {
    this.onOkMapper = onOkMapper;
    return this;
  }

  /** Set handler for 201 Created **/
  public RestRequester<T> onCreated(Function<Response, RestResult<T>> onCreatedMapper) {
    this.onCreatedMapper = onCreatedMapper;
    return this;
  }

  /** set handler for 202 Accepted **/
  public RestRequester<T> onAccepted(Function<Response, RestResult<T>> onAcceptedMapper) {
    this.onAcceptedMapper = onAcceptedMapper;
    return this;
  }

  /** set handler for 502 Bad Gateway **/
  public RestRequester<T> onBadGateway(Function<Response, RestResult<T>> onBadGatewayMapper) {
    this.onBadGatewayMapper = onBadGatewayMapper;
    return this;
  }

  /** set handler for 400 Bad Request **/
  public RestRequester<T> onBadRequest(Function<Response, RestResult<T>> onBadRequestMapper) {
    this.onBadRequestMapper = onBadRequestMapper;
    return this;
  }

  /** set handler for 409 Conflict **/
  public RestRequester<T> onConflict(Function<Response, RestResult<T>> onConflictMapper) {
    this.onConflictMapper = onConflictMapper;
    return this;
  }

  /** set handler for 417 Expectation Failed **/
  public RestRequester<T> onExpectationFailed(Function<Response, RestResult<T>> onExpectationFailedMapper) {
    this.onExpectationFailedMapper = onExpectationFailedMapper;
    return this;
  }

  /** set handler for 403 Forbidden **/
  public RestRequester<T> onForbidden(Function<Response, RestResult<T>> onForbiddenMapper) {
    this.onForbiddenMapper = onForbiddenMapper;
    return this;
  }

  /** set handler for 302 Found **/
  public RestRequester<T> onFound(Function<Response, RestResult<T>> onFoundMapper) {
    this.onFoundMapper = onFoundMapper;
    return this;
  }

  /** set handler for 504 Gateway Timeout **/
  public RestRequester<T> onGatewayTimeout(Function<Response, RestResult<T>> onGatewayTimeoutMapper) {
    this.onGatewayTimeoutMapper = onGatewayTimeoutMapper;
    return this;
  }

  /** set handler for 410 Gone **/
  public RestRequester<T> onGone(Function<Response, RestResult<T>> onGoneMapper) {
    this.onGoneMapper = onGoneMapper;
    return this;
  }

  /** set handler for 505 HTTP Version Not Supported **/
  public RestRequester<T> onHttpVersionNotSupported(Function<Response, RestResult<T>> onHttpVersionNotSupportedMapper) {
    this.onHttpVersionNotSupportedMapper = onHttpVersionNotSupportedMapper;
    return this;
  }

  /** set handler for 500 Internal Server Error **/
  public RestRequester<T> onInternalServerError(Function<Response, RestResult<T>> onInternalServerErrorMapper) {
    this.onInternalServerErrorMapper = onInternalServerErrorMapper;
    return this;
  }

  /** set handler for 411 Length Required **/
  public RestRequester<T> onLengthRequired(Function<Response, RestResult<T>> onLengthRequiredMapper) {
    this.onLengthRequiredMapper = onLengthRequiredMapper;
    return this;
  }

  /** set handler for 405 Method Not Allowed **/
  public RestRequester<T> onMethodNotAllowed(Function<Response, RestResult<T>> onMethodNotAllowedMapper) {
    this.onMethodNotAllowedMapper = onMethodNotAllowedMapper;
    return this;
  }

  /** set handler for 301 Moved Permanently **/
  public RestRequester<T> onMovedPermanently(Function<Response, RestResult<T>> onMovedPermanentlyMapper) {
    this.onMovedPermanentlyMapper = onMovedPermanentlyMapper;
    return this;
  }

  /** set handler for 204 No Content **/
  public RestRequester<T> onNoContent(Function<Response, RestResult<T>> onNoContentMapper) {
    this.onNoContentMapper = onNoContentMapper;
    return this;
  }

  /** set handler for 406 Not Acceptable **/
  public RestRequester<T> onNotAcceptable(Function<Response, RestResult<T>> onNotAcceptableMapper) {
    this.onNotAcceptableMapper = onNotAcceptableMapper;
    return this;
  }

  /** set handler for 404 Not Found **/
  public RestRequester<T> onNotFound(Function<Response, RestResult<T>> onNotFoundMapper) {
    this.onNotFoundMapper = onNotFoundMapper;
    return this;
  }

  /** set handler for 501 Not Implemented **/
  public RestRequester<T> onNotImplemented(Function<Response, RestResult<T>> onNotImplementedMapper) {
    this.onNotImplementedMapper = onNotImplementedMapper;
    return this;
  }

  /** set handler for 304 Not Modified **/
  public RestRequester<T> onNotModified(Function<Response, RestResult<T>> onNotModifiedMapper) {
    this.onNotModifiedMapper = onNotModifiedMapper;
    return this;
  }

  /** set handler for 206 Partial Content **/
  public RestRequester<T> onPartialContent(Function<Response, RestResult<T>> onPartialContentMapper) {
    this.onPartialContentMapper = onPartialContentMapper;
    return this;
  }

  /** set handler for 402 Payment Required **/
  public RestRequester<T> onPaymentRequired(Function<Response, RestResult<T>> onPaymentRequiredMapper) {
    this.onPaymentRequiredMapper = onPaymentRequiredMapper;
    return this;
  }

  /** set handler for 412 Precondition Failed **/
  public RestRequester<T> onPredonditionFailed(Function<Response, RestResult<T>> onPredonditionFailedMapper) {
    this.onPredonditionFailedMapper = onPredonditionFailedMapper;
    return this;
  }

  /** set handler for 407 Proxy Authentication Required **/
  public RestRequester<T> onProxyAuthenticationRequired(Function<Response, RestResult<T>> onProxyAuthenticationRequiredMapper) {
    this.onProxyAuthenticationRequiredMapper = onProxyAuthenticationRequiredMapper;
    return this;
  }

  /** set handler for 413 Request Entity Too Large **/
  public RestRequester<T> onRequestEntityTooLarge(Function<Response, RestResult<T>> onRequestEntityTooLargeMapper) {
    this.onRequestEntityTooLargeMapper = onRequestEntityTooLargeMapper;
    return this;
  }

  /** set handler for 408 Request Timeout **/
  public RestRequester<T> onRequestTimeout(Function<Response, RestResult<T>> onRequestTimeoutMapper) {
    this.onRequestTimeoutMapper = onRequestTimeoutMapper;
    return this;
  }

  /** set handler for 414 Request-URI Too Long **/
  public RestRequester<T> onRequestUriTooLong(Function<Response, RestResult<T>> onRequestUriTooLongMapper) {
    this.onRequestUriTooLongMapper = onRequestUriTooLongMapper;
    return this;
  }

  /** set handler for 416 Requested Range Not Satisfiable **/
  public RestRequester<T> onRequestedRangeNotSatisfiable(Function<Response, RestResult<T>> onRequestedRangeNotSatisfiableMapper) {
    this.onRequestedRangeNotSatisfiableMapper = onRequestedRangeNotSatisfiableMapper;
    return this;
  }

  /** set handler for 205 Reset Content **/
  public RestRequester<T> onResetContent(Function<Response, RestResult<T>> onResetContentMapper) {
    this.onResetContentMapper = onResetContentMapper;
    return this;
  }

  /** set handler for 303 See Other **/
  public RestRequester<T> onSeeOther(Function<Response, RestResult<T>> onSeeOtherMapper) {
    this.onSeeOtherMapper = onSeeOtherMapper;
    return this;
  }

  /** set handler for 503 Service Unavailable **/
  public RestRequester<T> onServiceUnavailable(Function<Response, RestResult<T>> onServiceUnavailableMapper) {
    this.onServiceUnavailableMapper = onServiceUnavailableMapper;
    return this;
  }

  /** set handler for 307 Temporary Redirect **/
  public RestRequester<T> onTemporaryRedirect(Function<Response, RestResult<T>> onTemporaryRedirectMapper) {
    this.onTemporaryRedirectMapper = onTemporaryRedirectMapper;
    return this;
  }

  /** set handler for 401 Unauthorized **/
  public RestRequester<T> onUnauthorized(Function<Response, RestResult<T>> onUnauthorizedMapper) {
    this.onUnauthorizedMapper = onUnauthorizedMapper;
    return this;
  }

  /** set handler for 415 Unsupported Media Type **/
  public RestRequester<T> onUnsupportedMediaType(Function<Response, RestResult<T>> onUnsupportedMediaTypeMapper) {
    this.onUnsupportedMediaTypeMapper = onUnsupportedMediaTypeMapper;
    return this;
  }

  /** set handler for 305 Use Proxy **/
  public RestRequester<T> onUseProxy(Function<Response, RestResult<T>> onUseProxyMapper) {
    this.onUseProxyMapper = onUseProxyMapper;
    return this;
  }

  public RestResult<T> getResult() {
    Map<Status, Function<Response, RestResult<T>>> statusCodeMappers = ImmutableMap.<Status, Function<Response, RestResult<T>>> builder()//
        .put(Status.ACCEPTED, onAcceptedMapper)//
        .put(Status.BAD_GATEWAY, onBadGatewayMapper)//
        .put(Status.BAD_REQUEST, onBadRequestMapper)//
        .put(Status.CONFLICT, onConflictMapper)//
        .put(Status.CREATED, onCreatedMapper)//
        .put(Status.EXPECTATION_FAILED, onExpectationFailedMapper)//
        .put(Status.FORBIDDEN, onForbiddenMapper)//
        .put(Status.FOUND, onFoundMapper)//
        .put(Status.GATEWAY_TIMEOUT, onGatewayTimeoutMapper)//
        .put(Status.GONE, onGoneMapper)//
        .put(Status.HTTP_VERSION_NOT_SUPPORTED, onHttpVersionNotSupportedMapper)//
        .put(Status.INTERNAL_SERVER_ERROR, onInternalServerErrorMapper)//
        .put(Status.LENGTH_REQUIRED, onLengthRequiredMapper)//
        .put(Status.METHOD_NOT_ALLOWED, onMethodNotAllowedMapper)//
        .put(Status.MOVED_PERMANENTLY, onMovedPermanentlyMapper)//
        .put(Status.NO_CONTENT, onNoContentMapper)//
        .put(Status.NOT_ACCEPTABLE, onNotAcceptableMapper)//
        .put(Status.NOT_FOUND, onNotFoundMapper)//
        .put(Status.NOT_IMPLEMENTED, onNotImplementedMapper)//
        .put(Status.NOT_MODIFIED, onNotModifiedMapper)//
        .put(Status.OK, onOkMapper)//
        .put(Status.PARTIAL_CONTENT, onPartialContentMapper)//
        .put(Status.PAYMENT_REQUIRED, onPaymentRequiredMapper)//
        .put(Status.PRECONDITION_FAILED, onPredonditionFailedMapper)//
        .put(Status.PROXY_AUTHENTICATION_REQUIRED, onProxyAuthenticationRequiredMapper)//
        .put(Status.REQUEST_ENTITY_TOO_LARGE, onRequestEntityTooLargeMapper)//
        .put(Status.REQUEST_TIMEOUT, onRequestTimeoutMapper)//
        .put(Status.REQUEST_URI_TOO_LONG, onRequestUriTooLongMapper)//
        .put(Status.REQUESTED_RANGE_NOT_SATISFIABLE, onRequestedRangeNotSatisfiableMapper)//
        .put(Status.RESET_CONTENT, onResetContentMapper)//
        .put(Status.SEE_OTHER, onSeeOtherMapper)//
        .put(Status.SERVICE_UNAVAILABLE, onServiceUnavailableMapper)//
        .put(Status.TEMPORARY_REDIRECT, onTemporaryRedirectMapper)//
        .put(Status.UNAUTHORIZED, onUnauthorizedMapper)//
        .put(Status.UNSUPPORTED_MEDIA_TYPE, onUnsupportedMediaTypeMapper)//
        .put(Status.USE_PROXY, onUseProxyMapper)//
        .build();

    RestResult<T> result = new RestResult<>();
    try {
      Response response = responseSupplier.get();
      Status status = Status.fromStatusCode(response.getStatus());

      if (statusCodeMappers.containsKey(status)) {
        return statusCodeMappers.get(status).apply(response);

      } else {
        return RestResult.failingResult(response);
      }

    } catch (Exception e) {
      e.printStackTrace();
      result.setFail(true);
    }

    return result;
  }
}
