package radiant.nimbus.api

import app.bsky.actor.GetPreferencesResponse
import app.bsky.actor.GetProfileQueryParams
import app.bsky.actor.GetProfileResponse
import app.bsky.actor.GetProfilesQueryParams
import app.bsky.actor.GetProfilesResponse
import app.bsky.actor.GetSuggestionsQueryParams
import app.bsky.actor.GetSuggestionsResponse
import app.bsky.actor.PutPreferencesRequest
import app.bsky.actor.SearchActorsQueryParams
import app.bsky.actor.SearchActorsResponse
import app.bsky.actor.SearchActorsTypeaheadQueryParams
import app.bsky.actor.SearchActorsTypeaheadResponse
import app.bsky.feed.DescribeFeedGeneratorResponse
import app.bsky.feed.GetActorFeedsQueryParams
import app.bsky.feed.GetActorFeedsResponse
import app.bsky.feed.GetActorLikesQueryParams
import app.bsky.feed.GetActorLikesResponse
import app.bsky.feed.GetAuthorFeedQueryParams
import app.bsky.feed.GetAuthorFeedResponse
import app.bsky.feed.GetFeedGeneratorQueryParams
import app.bsky.feed.GetFeedGeneratorResponse
import app.bsky.feed.GetFeedGeneratorsQueryParams
import app.bsky.feed.GetFeedGeneratorsResponse
import app.bsky.feed.GetFeedQueryParams
import app.bsky.feed.GetFeedResponse
import app.bsky.feed.GetFeedSkeletonQueryParams
import app.bsky.feed.GetFeedSkeletonResponse
import app.bsky.feed.GetLikesQueryParams
import app.bsky.feed.GetLikesResponse
import app.bsky.feed.GetListFeedQueryParams
import app.bsky.feed.GetListFeedResponse
import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponse
import app.bsky.feed.GetPostsQueryParams
import app.bsky.feed.GetPostsResponse
import app.bsky.feed.GetRepostedByQueryParams
import app.bsky.feed.GetRepostedByResponse
import app.bsky.feed.GetSuggestedFeedsQueryParams
import app.bsky.feed.GetSuggestedFeedsResponse
import app.bsky.feed.GetTimelineQueryParams
import app.bsky.feed.GetTimelineResponse
import app.bsky.feed.SearchPostsQueryParams
import app.bsky.feed.SearchPostsResponse
import app.bsky.graph.GetBlocksResponse
import app.bsky.graph.GetFollowersQueryParams
import app.bsky.graph.GetFollowersResponse
import app.bsky.graph.GetFollowsQueryParams
import app.bsky.graph.GetFollowsResponse
import app.bsky.graph.GetListBlocksQueryParams
import app.bsky.graph.GetListBlocksResponse
import app.bsky.graph.GetListMutesQueryParams
import app.bsky.graph.GetListMutesResponse
import app.bsky.graph.GetListQueryParams
import app.bsky.graph.GetListResponse
import app.bsky.graph.GetListsQueryParams
import app.bsky.graph.GetListsResponse
import app.bsky.graph.GetMutesQueryParams
import app.bsky.graph.GetMutesResponse
import app.bsky.graph.GetSuggestedFollowsByActorQueryParams
import app.bsky.graph.GetSuggestedFollowsByActorResponse
import app.bsky.graph.MuteActorListRequest
import app.bsky.graph.MuteActorRequest
import app.bsky.graph.UnmuteActorListRequest
import app.bsky.graph.UnmuteActorRequest
import app.bsky.notification.GetUnreadCountQueryParams
import app.bsky.notification.GetUnreadCountResponse
import app.bsky.notification.ListNotificationsQueryParams
import app.bsky.notification.ListNotificationsResponse
import app.bsky.notification.RegisterPushRequest
import app.bsky.notification.UpdateSeenRequest
import app.bsky.unspecced.ApplyLabelsRequest
import app.bsky.unspecced.GetPopularFeedGeneratorsQueryParams
import app.bsky.unspecced.GetPopularFeedGeneratorsResponse
import app.bsky.unspecced.GetPopularQueryParams
import app.bsky.unspecced.GetPopularResponse
import app.bsky.unspecced.GetTimelineSkeletonQueryParams
import app.bsky.unspecced.GetTimelineSkeletonResponse
import app.bsky.unspecced.SearchActorsSkeletonQueryParams
import app.bsky.unspecced.SearchActorsSkeletonResponse
import app.bsky.unspecced.SearchPostsSkeletonQueryParams
import app.bsky.unspecced.SearchPostsSkeletonResponse
import com.atproto.admin.DisableAccountInvitesRequest
import com.atproto.admin.DisableInviteCodesRequest
import com.atproto.admin.EnableAccountInvitesRequest
import com.atproto.admin.GetInviteCodesQueryParams
import com.atproto.admin.GetInviteCodesResponse
import com.atproto.admin.GetModerationActionQueryParams
import com.atproto.admin.GetModerationActionResponse
import com.atproto.admin.GetModerationActionsQueryParams
import com.atproto.admin.GetModerationActionsResponse
import com.atproto.admin.GetModerationReportQueryParams
import com.atproto.admin.GetModerationReportResponse
import com.atproto.admin.GetModerationReportsQueryParams
import com.atproto.admin.GetModerationReportsResponse
import com.atproto.admin.GetRepoResponse
import com.atproto.admin.ResolveModerationReportsRequest
import com.atproto.admin.ResolveModerationReportsResponse
import com.atproto.admin.ReverseModerationActionRequest
import com.atproto.admin.ReverseModerationActionResponse
import com.atproto.admin.SearchReposQueryParams
import com.atproto.admin.SearchReposResponse
import com.atproto.admin.SendEmailRequest
import com.atproto.admin.SendEmailResponse
import com.atproto.admin.TakeModerationActionRequest
import com.atproto.admin.TakeModerationActionResponse
import com.atproto.admin.UpdateAccountEmailRequest
import com.atproto.admin.UpdateAccountHandleRequest
import com.atproto.identity.ResolveHandleQueryParams
import com.atproto.identity.ResolveHandleResponse
import com.atproto.identity.UpdateHandleRequest
import com.atproto.label.QueryLabelsQueryParams
import com.atproto.label.QueryLabelsResponse
import com.atproto.label.SubscribeLabelsMessage
import com.atproto.label.SubscribeLabelsQueryParams
import com.atproto.moderation.CreateReportRequest
import com.atproto.moderation.CreateReportResponse
import com.atproto.repo.ApplyWritesRequest
import com.atproto.repo.CreateRecordRequest
import com.atproto.repo.CreateRecordResponse
import com.atproto.repo.DeleteRecordRequest
import com.atproto.repo.DescribeRepoQueryParams
import com.atproto.repo.DescribeRepoResponse
import com.atproto.repo.ListRecordsQueryParams
import com.atproto.repo.ListRecordsResponse
import com.atproto.repo.PutRecordRequest
import com.atproto.repo.PutRecordResponse
import com.atproto.repo.UploadBlobResponse
import com.atproto.server.ConfirmEmailRequest
import com.atproto.server.CreateAccountRequest
import com.atproto.server.CreateAccountResponse
import com.atproto.server.CreateAppPasswordRequest
import com.atproto.server.CreateAppPasswordResponse
import com.atproto.server.CreateInviteCodeRequest
import com.atproto.server.CreateInviteCodeResponse
import com.atproto.server.CreateInviteCodesRequest
import com.atproto.server.CreateInviteCodesResponse
import com.atproto.server.CreateSessionRequest
import com.atproto.server.CreateSessionResponse
import com.atproto.server.DeleteAccountRequest
import com.atproto.server.DescribeServerResponse
import com.atproto.server.GetAccountInviteCodesQueryParams
import com.atproto.server.GetAccountInviteCodesResponse
import com.atproto.server.GetSessionResponse
import com.atproto.server.ListAppPasswordsResponse
import com.atproto.server.RefreshSessionResponse
import com.atproto.server.RequestEmailUpdateResponse
import com.atproto.server.RequestPasswordResetRequest
import com.atproto.server.ResetPasswordRequest
import com.atproto.server.RevokeAppPasswordRequest
import com.atproto.server.UpdateEmailRequest
import com.atproto.sync.GetBlobQueryParams
import com.atproto.sync.GetCheckoutQueryParams
import com.atproto.sync.GetHeadQueryParams
import com.atproto.sync.GetHeadResponse
import com.atproto.sync.GetLatestCommitQueryParams
import com.atproto.sync.GetLatestCommitResponse
import com.atproto.sync.ListBlobsQueryParams
import com.atproto.sync.ListBlobsResponse
import com.atproto.sync.ListReposQueryParams
import com.atproto.sync.ListReposResponse
import com.atproto.sync.NotifyOfUpdateRequest
import com.atproto.sync.RequestCrawlRequest
import com.atproto.sync.SubscribeReposMessage
import com.atproto.sync.SubscribeReposQueryParams
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import kotlinx.coroutines.flow.Flow
import radiant.nimbus.api.auth.AuthInfo
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.api.xrpc.procedure
import radiant.nimbus.api.xrpc.query
import radiant.nimbus.api.xrpc.subscription
import radiant.nimbus.api.xrpc.toAtpResponse
import radiant.nimbus.api.xrpc.withXrpcConfiguration
import app.bsky.graph.GetBlocksQueryParams as GraphGetBlocksQueryParams
import com.atproto.admin.GetRecordQueryParams as AdminGetRecordQueryParams
import com.atproto.admin.GetRecordResponse as AdminGetRecordResponse
import com.atproto.admin.GetRepoQueryParams as AdminGetRepoQueryParams
import com.atproto.repo.GetRecordQueryParams as RepoGetRecordQueryParams
import com.atproto.repo.GetRecordResponse as RepoGetRecordResponse
import com.atproto.sync.GetBlocksQueryParams as SyncGetBlocksQueryParams
import com.atproto.sync.GetRecordQueryParams as SyncGetRecordQueryParams
import com.atproto.sync.GetRepoQueryParams as SyncGetRepoQueryParams

public class XrpcBlueskyApi(
  httpClient: HttpClient,
) : BlueskyApi {
  private val client: HttpClient = httpClient.withXrpcConfiguration()

  /**
   * Allow a labeler to apply labels directly.
   */
  override suspend fun applyLabels(request: ApplyLabelsRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/app.bsky.unspecced.applyLabels",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Apply a batch transaction of creates, updates, and deletes.
   */
  override suspend fun applyWrites(request: ApplyWritesRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.repo.applyWrites",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Confirm an email using a token from com.atproto.server.requestEmailConfirmation.
   */
  override suspend fun confirmEmail(request: ConfirmEmailRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.confirmEmail",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Create an account.
   */
  override suspend fun createAccount(request: CreateAccountRequest):
      AtpResponse<CreateAccountResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.createAccount",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Create an app-specific password.
   */
  override suspend fun createAppPassword(request: CreateAppPasswordRequest):
      AtpResponse<CreateAppPasswordResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.createAppPassword",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Create an invite code.
   */
  override suspend fun createInviteCode(request: CreateInviteCodeRequest):
      AtpResponse<CreateInviteCodeResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.createInviteCode",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Create an invite code.
   */
  override suspend fun createInviteCodes(request: CreateInviteCodesRequest):
      AtpResponse<CreateInviteCodesResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.createInviteCodes",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Create a new record.
   */
  override suspend fun createRecord(request: CreateRecordRequest):
      AtpResponse<CreateRecordResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.repo.createRecord",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Report a repo or a record.
   */
  override suspend fun createReport(request: CreateReportRequest):
      AtpResponse<CreateReportResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.moderation.createReport",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Create an authentication session.
   */
  override suspend fun createSession(request: CreateSessionRequest):
      AtpResponse<CreateSessionResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.createSession",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Delete a user account with a token and password.
   */
  override suspend fun deleteAccount(request: DeleteAccountRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.deleteAccount",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Delete a record, or ensure it doesn't exist.
   */
  override suspend fun deleteRecord(request: DeleteRecordRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.repo.deleteRecord",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Delete the current session.
   */
  override suspend fun deleteSession(): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.deleteSession",
    ).toAtpResponse()
  }

  /**
   * Returns information about a given feed generator including TOS & offered feed URIs
   */
  override suspend fun describeFeedGenerator(): AtpResponse<DescribeFeedGeneratorResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.describeFeedGenerator",
    ).toAtpResponse()
  }

  /**
   * Get information about the repo, including the list of collections.
   */
  override suspend fun describeRepo(params: DescribeRepoQueryParams):
      AtpResponse<DescribeRepoResponse> {
    return client.query(
      path = "/xrpc/com.atproto.repo.describeRepo",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Get a document describing the service's accounts configuration.
   */
  override suspend fun describeServer(): AtpResponse<DescribeServerResponse> {
    return client.query(
      path = "/xrpc/com.atproto.server.describeServer",
    ).toAtpResponse()
  }

  /**
   * Disable an account from receiving new invite codes, but does not invalidate existing codes
   */
  override suspend fun disableAccountInvites(request: DisableAccountInvitesRequest):
      AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.admin.disableAccountInvites",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Disable some set of codes and/or all codes associated with a set of users
   */
  override suspend fun disableInviteCodes(request: DisableInviteCodesRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.admin.disableInviteCodes",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Re-enable an accounts ability to receive invite codes
   */
  override suspend fun enableAccountInvites(request: EnableAccountInvitesRequest):
      AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.admin.enableAccountInvites",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Get all invite codes for a given account
   */
  override suspend fun getAccountInviteCodes(params: GetAccountInviteCodesQueryParams):
      AtpResponse<GetAccountInviteCodesResponse> {
    return client.query(
      path = "/xrpc/com.atproto.server.getAccountInviteCodes",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Retrieve a list of feeds created by a given actor
   */
  override suspend fun getActorFeeds(params: GetActorFeedsQueryParams):
      AtpResponse<GetActorFeedsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getActorFeeds",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * A view of the posts liked by an actor.
   */
  override suspend fun getActorLikes(params: GetActorLikesQueryParams):
      AtpResponse<GetActorLikesResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getActorLikes",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * A view of an actor's feed.
   */
  override suspend fun getAuthorFeed(params: GetAuthorFeedQueryParams):
      AtpResponse<GetAuthorFeedResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getAuthorFeed",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Get a blob associated with a given repo.
   */
  override suspend fun getBlob(params: GetBlobQueryParams): AtpResponse<ByteArray> {
    return client.query(
      path = "/xrpc/com.atproto.sync.getBlob",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Gets blocks from a given repo.
   */
  override suspend fun getBlocks(params: SyncGetBlocksQueryParams): AtpResponse<ByteArray> {
    return client.query(
      path = "/xrpc/com.atproto.sync.getBlocks",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Who is the requester's account blocking?
   */
  override suspend fun getBlocks(params: GraphGetBlocksQueryParams):
      AtpResponse<GetBlocksResponse> {
    return client.query(
      path = "/xrpc/app.bsky.graph.getBlocks",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * DEPRECATED - please use com.atproto.sync.getRepo instead
   */
  override suspend fun getCheckout(params: GetCheckoutQueryParams): AtpResponse<ByteArray> {
    return client.query(
      path = "/xrpc/com.atproto.sync.getCheckout",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Compose and hydrate a feed from a user's selected feed generator
   */
  override suspend fun getFeed(params: GetFeedQueryParams): AtpResponse<GetFeedResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getFeed",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Get information about a specific feed offered by a feed generator, such as its online status
   */
  override suspend fun getFeedGenerator(params: GetFeedGeneratorQueryParams):
      AtpResponse<GetFeedGeneratorResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getFeedGenerator",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Get information about a list of feed generators
   */
  override suspend fun getFeedGenerators(params: GetFeedGeneratorsQueryParams):
      AtpResponse<GetFeedGeneratorsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getFeedGenerators",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * A skeleton of a feed provided by a feed generator
   */
  override suspend fun getFeedSkeleton(params: GetFeedSkeletonQueryParams):
      AtpResponse<GetFeedSkeletonResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getFeedSkeleton",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Who is following an actor?
   */
  override suspend fun getFollowers(params: GetFollowersQueryParams):
      AtpResponse<GetFollowersResponse> {
    return client.query(
      path = "/xrpc/app.bsky.graph.getFollowers",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Who is an actor following?
   */
  override suspend fun getFollows(params: GetFollowsQueryParams): AtpResponse<GetFollowsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.graph.getFollows",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * DEPRECATED - please use com.atproto.sync.getLatestCommit instead
   */
  override suspend fun getHead(params: GetHeadQueryParams): AtpResponse<GetHeadResponse> {
    return client.query(
      path = "/xrpc/com.atproto.sync.getHead",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Admin view of invite codes
   */
  override suspend fun getInviteCodes(params: GetInviteCodesQueryParams):
      AtpResponse<GetInviteCodesResponse> {
    return client.query(
      path = "/xrpc/com.atproto.admin.getInviteCodes",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Gets the current commit CID & revision of the repo.
   */
  override suspend fun getLatestCommit(params: GetLatestCommitQueryParams):
      AtpResponse<GetLatestCommitResponse> {
    return client.query(
      path = "/xrpc/com.atproto.sync.getLatestCommit",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  override suspend fun getLikes(params: GetLikesQueryParams): AtpResponse<GetLikesResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getLikes",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Fetch a list of actors
   */
  override suspend fun getList(params: GetListQueryParams): AtpResponse<GetListResponse> {
    return client.query(
      path = "/xrpc/app.bsky.graph.getList",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Which lists is the requester's account blocking?
   */
  override suspend fun getListBlocks(params: GetListBlocksQueryParams):
      AtpResponse<GetListBlocksResponse> {
    return client.query(
      path = "/xrpc/app.bsky.graph.getListBlocks",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * A view of a recent posts from actors in a list
   */
  override suspend fun getListFeed(params: GetListFeedQueryParams):
      AtpResponse<GetListFeedResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getListFeed",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Which lists is the requester's account muting?
   */
  override suspend fun getListMutes(params: GetListMutesQueryParams):
      AtpResponse<GetListMutesResponse> {
    return client.query(
      path = "/xrpc/app.bsky.graph.getListMutes",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Fetch a list of lists that belong to an actor
   */
  override suspend fun getLists(params: GetListsQueryParams): AtpResponse<GetListsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.graph.getLists",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * View details about a moderation action.
   */
  override suspend fun getModerationAction(params: GetModerationActionQueryParams):
      AtpResponse<GetModerationActionResponse> {
    return client.query(
      path = "/xrpc/com.atproto.admin.getModerationAction",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * List moderation actions related to a subject.
   */
  override suspend fun getModerationActions(params: GetModerationActionsQueryParams):
      AtpResponse<GetModerationActionsResponse> {
    return client.query(
      path = "/xrpc/com.atproto.admin.getModerationActions",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * View details about a moderation report.
   */
  override suspend fun getModerationReport(params: GetModerationReportQueryParams):
      AtpResponse<GetModerationReportResponse> {
    return client.query(
      path = "/xrpc/com.atproto.admin.getModerationReport",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * List moderation reports related to a subject.
   */
  override suspend fun getModerationReports(params: GetModerationReportsQueryParams):
      AtpResponse<GetModerationReportsResponse> {
    return client.query(
      path = "/xrpc/com.atproto.admin.getModerationReports",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Who does the viewer mute?
   */
  override suspend fun getMutes(params: GetMutesQueryParams): AtpResponse<GetMutesResponse> {
    return client.query(
      path = "/xrpc/app.bsky.graph.getMutes",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * DEPRECATED: will be removed soon, please find a feed generator alternative
   */
  override suspend fun getPopular(params: GetPopularQueryParams): AtpResponse<GetPopularResponse> {
    return client.query(
      path = "/xrpc/app.bsky.unspecced.getPopular",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * An unspecced view of globally popular feed generators
   */
  override suspend fun getPopularFeedGenerators(params: GetPopularFeedGeneratorsQueryParams):
      AtpResponse<GetPopularFeedGeneratorsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.unspecced.getPopularFeedGenerators",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  override suspend fun getPostThread(params: GetPostThreadQueryParams):
      AtpResponse<GetPostThreadResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getPostThread",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * A view of an actor's feed.
   */
  override suspend fun getPosts(params: GetPostsQueryParams): AtpResponse<GetPostsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getPosts",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Get private preferences attached to the account.
   */
  override suspend fun getPreferences(): AtpResponse<GetPreferencesResponse> {
    return client.query(
      path = "/xrpc/app.bsky.actor.getPreferences",
    ).toAtpResponse()
  }

  override suspend fun getProfile(params: GetProfileQueryParams): AtpResponse<GetProfileResponse> {
    return client.query(
      path = "/xrpc/app.bsky.actor.getProfile",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  override suspend fun getProfiles(params: GetProfilesQueryParams):
      AtpResponse<GetProfilesResponse> {
    return client.query(
      path = "/xrpc/app.bsky.actor.getProfiles",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Get a record.
   */
  override suspend fun getRecord(params: RepoGetRecordQueryParams):
      AtpResponse<RepoGetRecordResponse> {
    return client.query(
      path = "/xrpc/com.atproto.repo.getRecord",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Gets blocks needed for existence or non-existence of record.
   */
  override suspend fun getRecord(params: SyncGetRecordQueryParams): AtpResponse<ByteArray> {
    return client.query(
      path = "/xrpc/com.atproto.sync.getRecord",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * View details about a record.
   */
  override suspend fun getRecord(params: AdminGetRecordQueryParams):
      AtpResponse<AdminGetRecordResponse> {
    return client.query(
      path = "/xrpc/com.atproto.admin.getRecord",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Gets the did's repo, optionally catching up from a specific revision.
   */
  override suspend fun getRepo(params: SyncGetRepoQueryParams): AtpResponse<ByteArray> {
    return client.query(
      path = "/xrpc/com.atproto.sync.getRepo",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * View details about a repository.
   */
  override suspend fun getRepo(params: AdminGetRepoQueryParams): AtpResponse<GetRepoResponse> {
    return client.query(
      path = "/xrpc/com.atproto.admin.getRepo",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  override suspend fun getRepostedBy(params: GetRepostedByQueryParams):
      AtpResponse<GetRepostedByResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getRepostedBy",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Get information about the current session.
   */
  override suspend fun getSession(): AtpResponse<GetSessionResponse> {
    return client.query(
      path = "/xrpc/com.atproto.server.getSession",
    ).toAtpResponse()
  }

  /**
   * Get a list of suggested feeds for the viewer.
   */
  override suspend fun getSuggestedFeeds(params: GetSuggestedFeedsQueryParams):
      AtpResponse<GetSuggestedFeedsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getSuggestedFeeds",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Get suggested follows related to a given actor.
   */
  override suspend fun getSuggestedFollowsByActor(params: GetSuggestedFollowsByActorQueryParams):
      AtpResponse<GetSuggestedFollowsByActorResponse> {
    return client.query(
      path = "/xrpc/app.bsky.graph.getSuggestedFollowsByActor",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Get a list of actors suggested for following. Used in discovery UIs.
   */
  override suspend fun getSuggestions(params: GetSuggestionsQueryParams):
      AtpResponse<GetSuggestionsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.actor.getSuggestions",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * A view of the user's home timeline.
   */
  override suspend fun getTimeline(params: GetTimelineQueryParams):
      AtpResponse<GetTimelineResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.getTimeline",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * A skeleton of a timeline - UNSPECCED & WILL GO AWAY SOON
   */
  override suspend fun getTimelineSkeleton(params: GetTimelineSkeletonQueryParams):
      AtpResponse<GetTimelineSkeletonResponse> {
    return client.query(
      path = "/xrpc/app.bsky.unspecced.getTimelineSkeleton",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  override suspend fun getUnreadCount(params: GetUnreadCountQueryParams):
      AtpResponse<GetUnreadCountResponse> {
    return client.query(
      path = "/xrpc/app.bsky.notification.getUnreadCount",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * List all app-specific passwords.
   */
  override suspend fun listAppPasswords(): AtpResponse<ListAppPasswordsResponse> {
    return client.query(
      path = "/xrpc/com.atproto.server.listAppPasswords",
    ).toAtpResponse()
  }

  /**
   * List blob cids since some revision
   */
  override suspend fun listBlobs(params: ListBlobsQueryParams): AtpResponse<ListBlobsResponse> {
    return client.query(
      path = "/xrpc/com.atproto.sync.listBlobs",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  override suspend fun listNotifications(params: ListNotificationsQueryParams):
      AtpResponse<ListNotificationsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.notification.listNotifications",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * List a range of records in a collection.
   */
  override suspend fun listRecords(params: ListRecordsQueryParams):
      AtpResponse<ListRecordsResponse> {
    return client.query(
      path = "/xrpc/com.atproto.repo.listRecords",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * List dids and root cids of hosted repos
   */
  override suspend fun listRepos(params: ListReposQueryParams): AtpResponse<ListReposResponse> {
    return client.query(
      path = "/xrpc/com.atproto.sync.listRepos",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Mute an actor by did or handle.
   */
  override suspend fun muteActor(request: MuteActorRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/app.bsky.graph.muteActor",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Mute a list of actors.
   */
  override suspend fun muteActorList(request: MuteActorListRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/app.bsky.graph.muteActorList",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Notify a crawling service of a recent update. Often when a long break between updates causes
   * the connection with the crawling service to break.
   */
  override suspend fun notifyOfUpdate(request: NotifyOfUpdateRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.sync.notifyOfUpdate",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Sets the private preferences attached to the account.
   */
  override suspend fun putPreferences(request: PutPreferencesRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/app.bsky.actor.putPreferences",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Write a record, creating or updating it as needed.
   */
  override suspend fun putRecord(request: PutRecordRequest): AtpResponse<PutRecordResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.repo.putRecord",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Find labels relevant to the provided URI patterns.
   */
  override suspend fun queryLabels(params: QueryLabelsQueryParams):
      AtpResponse<QueryLabelsResponse> {
    return client.query(
      path = "/xrpc/com.atproto.label.queryLabels",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Refresh an authentication session.
   */
  override suspend fun refreshSession(): AtpResponse<RefreshSessionResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.refreshSession",
    ).toAtpResponse()
  }

  override suspend fun refreshSession(auth: AuthInfo): AtpResponse<RefreshSessionResponse> {
    return client.post("/xrpc/com.atproto.server.refreshSession") {
      this.bearerAuth(auth.refreshJwt)
    }.toAtpResponse()
  }

  /**
   * Register for push notifications with a service
   */
  override suspend fun registerPush(request: RegisterPushRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/app.bsky.notification.registerPush",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Initiate a user account deletion via email.
   */
  override suspend fun requestAccountDelete(): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.requestAccountDelete",
    ).toAtpResponse()
  }

  /**
   * Request a service to persistently crawl hosted repos.
   */
  override suspend fun requestCrawl(request: RequestCrawlRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.sync.requestCrawl",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Request an email with a code to confirm ownership of email
   */
  override suspend fun requestEmailConfirmation(): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.requestEmailConfirmation",
    ).toAtpResponse()
  }

  /**
   * Request a token in order to update email.
   */
  override suspend fun requestEmailUpdate(): AtpResponse<RequestEmailUpdateResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.requestEmailUpdate",
    ).toAtpResponse()
  }

  /**
   * Initiate a user account password reset via email.
   */
  override suspend fun requestPasswordReset(request: RequestPasswordResetRequest):
      AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.requestPasswordReset",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Reset a user account password using a token.
   */
  override suspend fun resetPassword(request: ResetPasswordRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.resetPassword",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Provides the DID of a repo.
   */
  override suspend fun resolveHandle(params: ResolveHandleQueryParams):
      AtpResponse<ResolveHandleResponse> {
    return client.query(
      path = "/xrpc/com.atproto.identity.resolveHandle",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Resolve moderation reports by an action.
   */
  override suspend fun resolveModerationReports(request: ResolveModerationReportsRequest):
      AtpResponse<ResolveModerationReportsResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.admin.resolveModerationReports",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Reverse a moderation action.
   */
  override suspend fun reverseModerationAction(request: ReverseModerationActionRequest):
      AtpResponse<ReverseModerationActionResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.admin.reverseModerationAction",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Revoke an app-specific password by name.
   */
  override suspend fun revokeAppPassword(request: RevokeAppPasswordRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.revokeAppPassword",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Find actors (profiles) matching search criteria.
   */
  override suspend fun searchActors(params: SearchActorsQueryParams):
      AtpResponse<SearchActorsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.actor.searchActors",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Backend Actors (profile) search, returning only skeleton
   */
  override suspend fun searchActorsSkeleton(params: SearchActorsSkeletonQueryParams):
      AtpResponse<SearchActorsSkeletonResponse> {
    return client.query(
      path = "/xrpc/app.bsky.unspecced.searchActorsSkeleton",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Find actor suggestions for a search term.
   */
  override suspend fun searchActorsTypeahead(params: SearchActorsTypeaheadQueryParams):
      AtpResponse<SearchActorsTypeaheadResponse> {
    return client.query(
      path = "/xrpc/app.bsky.actor.searchActorsTypeahead",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Find posts matching search criteria
   */
  override suspend fun searchPosts(params: SearchPostsQueryParams):
      AtpResponse<SearchPostsResponse> {
    return client.query(
      path = "/xrpc/app.bsky.feed.searchPosts",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Backend Posts search, returning only skeleton
   */
  override suspend fun searchPostsSkeleton(params: SearchPostsSkeletonQueryParams):
      AtpResponse<SearchPostsSkeletonResponse> {
    return client.query(
      path = "/xrpc/app.bsky.unspecced.searchPostsSkeleton",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Find repositories based on a search term.
   */
  override suspend fun searchRepos(params: SearchReposQueryParams):
      AtpResponse<SearchReposResponse> {
    return client.query(
      path = "/xrpc/com.atproto.admin.searchRepos",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Send email to a user's primary email address
   */
  override suspend fun sendEmail(request: SendEmailRequest): AtpResponse<SendEmailResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.admin.sendEmail",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Subscribe to label updates
   */
  override suspend fun subscribeLabels(params: SubscribeLabelsQueryParams):
      Flow<AtpResponse<SubscribeLabelsMessage>> {
    return client.subscription(
      path = "/xrpc/com.atproto.label.subscribeLabels",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Subscribe to repo updates
   */
  override suspend fun subscribeRepos(params: SubscribeReposQueryParams):
      Flow<AtpResponse<SubscribeReposMessage>> {
    return client.subscription(
      path = "/xrpc/com.atproto.sync.subscribeRepos",
      queryParams = params.asList(),
    ).toAtpResponse()
  }

  /**
   * Take a moderation action on a repo.
   */
  override suspend fun takeModerationAction(request: TakeModerationActionRequest):
      AtpResponse<TakeModerationActionResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.admin.takeModerationAction",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Unmute an actor by did or handle.
   */
  override suspend fun unmuteActor(request: UnmuteActorRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/app.bsky.graph.unmuteActor",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Unmute a list of actors.
   */
  override suspend fun unmuteActorList(request: UnmuteActorListRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/app.bsky.graph.unmuteActorList",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Administrative action to update an account's email
   */
  override suspend fun updateAccountEmail(request: UpdateAccountEmailRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.admin.updateAccountEmail",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Administrative action to update an account's handle
   */
  override suspend fun updateAccountHandle(request: UpdateAccountHandleRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.admin.updateAccountHandle",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Update an account's email.
   */
  override suspend fun updateEmail(request: UpdateEmailRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.server.updateEmail",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Updates the handle of the account
   */
  override suspend fun updateHandle(request: UpdateHandleRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/com.atproto.identity.updateHandle",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Notify server that the user has seen notifications.
   */
  override suspend fun updateSeen(request: UpdateSeenRequest): AtpResponse<Unit> {
    return client.procedure(
      path = "/xrpc/app.bsky.notification.updateSeen",
      body = request,
      encoding = "application/json",
    ).toAtpResponse()
  }

  /**
   * Upload a new blob to be added to repo in a later request.
   */
  override suspend fun uploadBlob(request: ByteArray): AtpResponse<UploadBlobResponse> {
    return client.procedure(
      path = "/xrpc/com.atproto.repo.uploadBlob",
      body = request,
      encoding = "*/*",
    ).toAtpResponse()
  }
}
