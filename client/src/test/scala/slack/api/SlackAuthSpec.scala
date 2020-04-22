package slack.api

import slack.api.auth.{ test => slackTest, testAuth => slackTestAuth }
import zio.test._
import Assertion._
import TestAspect._
import slack.SlackError
import slack.core.client.SlackClient
import slack.models.AuthIdentity
import sttp.client.{ Request, Response, SttpBackend }
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.zio.{ AsyncHttpClientZioBackend, SttpClient }
import sttp.client.monad.MonadError
import sttp.client.ws.WebSocketResponse
import zio.{ Has, Layer, Task, ZLayer }

case class HijackingBackend(delegate: SttpBackend[Task, Nothing, WebSocketHandler])
    extends SttpBackend[Task, Nothing, WebSocketHandler] {
  override def send[T](request: Request[T, Nothing]): Task[Response[T]] =
    delegate.send(request)

  override def openWebsocket[T, WS_RESULT](request: Request[T, Nothing],
                                           handler: WebSocketHandler[WS_RESULT]): Task[WebSocketResponse[WS_RESULT]] =
    delegate.openWebsocket(request, handler)

  override def close(): Task[Unit] = delegate.close()

  override def responseMonad: MonadError[Task] = delegate.responseMonad
}

//class SlackAuthSpec extends DefaultRunnableSpec {
//  val client: ZLayer[Any, Throwable, SttpClient] = AsyncHttpClientZioBackend.layer().map { client =>
//    Has(HijackingBackend(client.get[SttpBackend[Task, Nothing, WebSocketHandler]]))
//  }
//  val token: Layer[Nothing, ClientToken] = AccessToken.live("abc123")
//
//  val spec =
//    suite("Auth")(
//      testM("test OK") {
//        assertM(slackTest)(isTrue)
//      }.provideCustomLayerShared((client >>> SlackClient.live) ++ token),
//      testM("testAuth OK") {
//        assertM(slackTestAuth)(equalTo(AuthIdentity("", "", "", "", "")))
//      }.provideCustomLayerShared((client >>> SlackClient.live) ++ token)
//    )
//}
