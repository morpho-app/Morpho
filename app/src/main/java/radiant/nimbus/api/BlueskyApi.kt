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
import kotlinx.coroutines.flow.Flow
import radiant.nimbus.api.auth.AuthInfo
import radiant.nimbus.api.response.AtpResponse
import app.bsky.graph.GetBlocksQueryParams as GraphGetBlocksQueryParams
import com.atproto.admin.GetRecordQueryParams as AdminGetRecordQueryParams
import com.atproto.admin.GetRecordResponse as AdminGetRecordResponse
import com.atproto.admin.GetRepoQueryParams as AdminGetRepoQueryParams
import com.atproto.repo.GetRecordQueryParams as RepoGetRecordQueryParams
import com.atproto.repo.GetRecordResponse as RepoGetRecordResponse
import com.atproto.sync.GetBlocksQueryParams as SyncGetBlocksQueryParams
import com.atproto.sync.GetRecordQueryParams as SyncGetRecordQueryParams
import com.atproto.sync.GetRepoQueryParams as SyncGetRepoQueryParams

public interface BlueskyApi {
  /**
   * Allow a labeler to apply labels directly.
   */
  public suspend fun applyLabels(request: ApplyLabelsRequest): AtpResponse<Unit>

  /**
   * Apply a batch transaction of creates, updates, and deletes.
   */
  public suspend fun applyWrites(request: ApplyWritesRequest): AtpResponse<Unit>

  /**
   * Confirm an email using a token from com.atproto.server.requestEmailConfirmation.
   */
  public suspend fun confirmEmail(request: ConfirmEmailRequest): AtpResponse<Unit>

  /**
   * Create an account.
   */
  public suspend fun createAccount(request: CreateAccountRequest):
      AtpResponse<CreateAccountResponse>

  /**
   * Create an app-specific password.
   */
  public suspend fun createAppPassword(request: CreateAppPasswordRequest):
      AtpResponse<CreateAppPasswordResponse>

  /**
   * Create an invite code.
   */
  public suspend fun createInviteCode(request: CreateInviteCodeRequest):
      AtpResponse<CreateInviteCodeResponse>

  /**
   * Create an invite code.
   */
  public suspend fun createInviteCodes(request: CreateInviteCodesRequest):
      AtpResponse<CreateInviteCodesResponse>

  /**
   * Create a new record.
   */
  public suspend fun createRecord(request: CreateRecordRequest): AtpResponse<CreateRecordResponse>

  /**
   * Report a repo or a record.
   */
  public suspend fun createReport(request: CreateReportRequest): AtpResponse<CreateReportResponse>

  /**
   * Create an authentication session.
   */
  public suspend fun createSession(request: CreateSessionRequest):
      AtpResponse<CreateSessionResponse>

  /**
   * Delete a user account with a token and password.
   */
  public suspend fun deleteAccount(request: DeleteAccountRequest): AtpResponse<Unit>

  /**
   * Delete a record, or ensure it doesn't exist.
   */
  public suspend fun deleteRecord(request: DeleteRecordRequest): AtpResponse<Unit>

  /**
   * Delete the current session.
   */
  public suspend fun deleteSession(): AtpResponse<Unit>

  /**
   * Returns information about a given feed generator including TOS & offered feed URIs
   */
  public suspend fun describeFeedGenerator(): AtpResponse<DescribeFeedGeneratorResponse>

  /**
   * Get information about the repo, including the list of collections.
   */
  public suspend fun describeRepo(params: DescribeRepoQueryParams):
      AtpResponse<DescribeRepoResponse>

  /**
   * Get a document describing the service's accounts configuration.
   */
  public suspend fun describeServer(): AtpResponse<DescribeServerResponse>

  /**
   * Disable an account from receiving new invite codes, but does not invalidate existing codes
   */
  public suspend fun disableAccountInvites(request: DisableAccountInvitesRequest): AtpResponse<Unit>

  /**
   * Disable some set of codes and/or all codes associated with a set of users
   */
  public suspend fun disableInviteCodes(request: DisableInviteCodesRequest): AtpResponse<Unit>

  /**
   * Re-enable an accounts ability to receive invite codes
   */
  public suspend fun enableAccountInvites(request: EnableAccountInvitesRequest): AtpResponse<Unit>

  /**
   * Get all invite codes for a given account
   */
  public suspend fun getAccountInviteCodes(params: GetAccountInviteCodesQueryParams):
      AtpResponse<GetAccountInviteCodesResponse>

  /**
   * Retrieve a list of feeds created by a given actor
   */
  public suspend fun getActorFeeds(params: GetActorFeedsQueryParams):
      AtpResponse<GetActorFeedsResponse>

  /**
   * A view of the posts liked by an actor.
   */
  public suspend fun getActorLikes(params: GetActorLikesQueryParams):
      AtpResponse<GetActorLikesResponse>

  /**
   * A view of an actor's feed.
   */
  public suspend fun getAuthorFeed(params: GetAuthorFeedQueryParams):
      AtpResponse<GetAuthorFeedResponse>

  /**
   * Get a blob associated with a given repo.
   */
  public suspend fun getBlob(params: GetBlobQueryParams): AtpResponse<ByteArray>

  /**
   * Gets blocks from a given repo.
   */
  public suspend fun getBlocks(params: SyncGetBlocksQueryParams): AtpResponse<ByteArray>

  /**
   * Who is the requester's account blocking?
   */
  public suspend fun getBlocks(params: GraphGetBlocksQueryParams): AtpResponse<GetBlocksResponse>

  /**
   * DEPRECATED - please use com.atproto.sync.getRepo instead
   */
  public suspend fun getCheckout(params: GetCheckoutQueryParams): AtpResponse<ByteArray>

  /**
   * Compose and hydrate a feed from a user's selected feed generator
   */
  public suspend fun getFeed(params: GetFeedQueryParams): AtpResponse<GetFeedResponse>

  /**
   * Get information about a specific feed offered by a feed generator, such as its online status
   */
  public suspend fun getFeedGenerator(params: GetFeedGeneratorQueryParams):
      AtpResponse<GetFeedGeneratorResponse>

  /**
   * Get information about a list of feed generators
   */
  public suspend fun getFeedGenerators(params: GetFeedGeneratorsQueryParams):
      AtpResponse<GetFeedGeneratorsResponse>

  /**
   * A skeleton of a feed provided by a feed generator
   */
  public suspend fun getFeedSkeleton(params: GetFeedSkeletonQueryParams):
      AtpResponse<GetFeedSkeletonResponse>

  /**
   * Who is following an actor?
   */
  public suspend fun getFollowers(params: GetFollowersQueryParams):
      AtpResponse<GetFollowersResponse>

  /**
   * Who is an actor following?
   */
  public suspend fun getFollows(params: GetFollowsQueryParams): AtpResponse<GetFollowsResponse>

  /**
   * DEPRECATED - please use com.atproto.sync.getLatestCommit instead
   */
  public suspend fun getHead(params: GetHeadQueryParams): AtpResponse<GetHeadResponse>

  /**
   * Admin view of invite codes
   */
  public suspend fun getInviteCodes(params: GetInviteCodesQueryParams):
      AtpResponse<GetInviteCodesResponse>

  /**
   * Gets the current commit CID & revision of the repo.
   */
  public suspend fun getLatestCommit(params: GetLatestCommitQueryParams):
      AtpResponse<GetLatestCommitResponse>

  public suspend fun getLikes(params: GetLikesQueryParams): AtpResponse<GetLikesResponse>

  /**
   * Fetch a list of actors
   */
  public suspend fun getList(params: GetListQueryParams): AtpResponse<GetListResponse>

  /**
   * Which lists is the requester's account blocking?
   */
  public suspend fun getListBlocks(params: GetListBlocksQueryParams):
      AtpResponse<GetListBlocksResponse>

  /**
   * A view of a recent posts from actors in a list
   */
  public suspend fun getListFeed(params: GetListFeedQueryParams): AtpResponse<GetListFeedResponse>

  /**
   * Which lists is the requester's account muting?
   */
  public suspend fun getListMutes(params: GetListMutesQueryParams):
      AtpResponse<GetListMutesResponse>

  /**
   * Fetch a list of lists that belong to an actor
   */
  public suspend fun getLists(params: GetListsQueryParams): AtpResponse<GetListsResponse>

  /**
   * View details about a moderation action.
   */
  public suspend fun getModerationAction(params: GetModerationActionQueryParams):
      AtpResponse<GetModerationActionResponse>

  /**
   * List moderation actions related to a subject.
   */
  public suspend fun getModerationActions(params: GetModerationActionsQueryParams):
      AtpResponse<GetModerationActionsResponse>

  /**
   * View details about a moderation report.
   */
  public suspend fun getModerationReport(params: GetModerationReportQueryParams):
      AtpResponse<GetModerationReportResponse>

  /**
   * List moderation reports related to a subject.
   */
  public suspend fun getModerationReports(params: GetModerationReportsQueryParams):
      AtpResponse<GetModerationReportsResponse>

  /**
   * Who does the viewer mute?
   */
  public suspend fun getMutes(params: GetMutesQueryParams): AtpResponse<GetMutesResponse>

  /**
   * DEPRECATED: will be removed soon, please find a feed generator alternative
   */
  public suspend fun getPopular(params: GetPopularQueryParams): AtpResponse<GetPopularResponse>

  /**
   * An unspecced view of globally popular feed generators
   */
  public suspend fun getPopularFeedGenerators(params: GetPopularFeedGeneratorsQueryParams):
      AtpResponse<GetPopularFeedGeneratorsResponse>

  public suspend fun getPostThread(params: GetPostThreadQueryParams):
      AtpResponse<GetPostThreadResponse>

  /**
   * A view of an actor's feed.
   */
  public suspend fun getPosts(params: GetPostsQueryParams): AtpResponse<GetPostsResponse>

  /**
   * Get private preferences attached to the account.
   */
  public suspend fun getPreferences(): AtpResponse<GetPreferencesResponse>

  public suspend fun getProfile(params: GetProfileQueryParams): AtpResponse<GetProfileResponse>

  public suspend fun getProfiles(params: GetProfilesQueryParams): AtpResponse<GetProfilesResponse>

  /**
   * Get a record.
   */
  public suspend fun getRecord(params: RepoGetRecordQueryParams): AtpResponse<RepoGetRecordResponse>

  /**
   * Gets blocks needed for existence or non-existence of record.
   */
  public suspend fun getRecord(params: SyncGetRecordQueryParams): AtpResponse<ByteArray>

  /**
   * View details about a record.
   */
  public suspend fun getRecord(params: AdminGetRecordQueryParams):
      AtpResponse<AdminGetRecordResponse>

  /**
   * Gets the did's repo, optionally catching up from a specific revision.
   */
  public suspend fun getRepo(params: SyncGetRepoQueryParams): AtpResponse<ByteArray>

  /**
   * View details about a repository.
   */
  public suspend fun getRepo(params: AdminGetRepoQueryParams): AtpResponse<GetRepoResponse>

  public suspend fun getRepostedBy(params: GetRepostedByQueryParams):
      AtpResponse<GetRepostedByResponse>

  /**
   * Get information about the current session.
   */
  public suspend fun getSession(): AtpResponse<GetSessionResponse>

  /**
   * Get a list of suggested feeds for the viewer.
   */
  public suspend fun getSuggestedFeeds(params: GetSuggestedFeedsQueryParams):
      AtpResponse<GetSuggestedFeedsResponse>

  /**
   * Get suggested follows related to a given actor.
   */
  public suspend fun getSuggestedFollowsByActor(params: GetSuggestedFollowsByActorQueryParams):
      AtpResponse<GetSuggestedFollowsByActorResponse>

  /**
   * Get a list of actors suggested for following. Used in discovery UIs.
   */
  public suspend fun getSuggestions(params: GetSuggestionsQueryParams):
      AtpResponse<GetSuggestionsResponse>

  /**
   * A view of the user's home timeline.
   */
  public suspend fun getTimeline(params: GetTimelineQueryParams): AtpResponse<GetTimelineResponse>

  /**
   * A skeleton of a timeline - UNSPECCED & WILL GO AWAY SOON
   */
  public suspend fun getTimelineSkeleton(params: GetTimelineSkeletonQueryParams):
      AtpResponse<GetTimelineSkeletonResponse>

  public suspend fun getUnreadCount(params: GetUnreadCountQueryParams):
      AtpResponse<GetUnreadCountResponse>

  /**
   * List all app-specific passwords.
   */
  public suspend fun listAppPasswords(): AtpResponse<ListAppPasswordsResponse>

  /**
   * List blob cids since some revision
   */
  public suspend fun listBlobs(params: ListBlobsQueryParams): AtpResponse<ListBlobsResponse>

  public suspend fun listNotifications(params: ListNotificationsQueryParams):
      AtpResponse<ListNotificationsResponse>

  /**
   * List a range of records in a collection.
   */
  public suspend fun listRecords(params: ListRecordsQueryParams): AtpResponse<ListRecordsResponse>

  /**
   * List dids and root cids of hosted repos
   */
  public suspend fun listRepos(params: ListReposQueryParams): AtpResponse<ListReposResponse>

  /**
   * Mute an actor by did or handle.
   */
  public suspend fun muteActor(request: MuteActorRequest): AtpResponse<Unit>

  /**
   * Mute a list of actors.
   */
  public suspend fun muteActorList(request: MuteActorListRequest): AtpResponse<Unit>

  /**
   * Notify a crawling service of a recent update. Often when a long break between updates causes
   * the connection with the crawling service to break.
   */
  public suspend fun notifyOfUpdate(request: NotifyOfUpdateRequest): AtpResponse<Unit>

  /**
   * Sets the private preferences attached to the account.
   */
  public suspend fun putPreferences(request: PutPreferencesRequest): AtpResponse<Unit>

  /**
   * Write a record, creating or updating it as needed.
   */
  public suspend fun putRecord(request: PutRecordRequest): AtpResponse<PutRecordResponse>

  /**
   * Find labels relevant to the provided URI patterns.
   */
  public suspend fun queryLabels(params: QueryLabelsQueryParams): AtpResponse<QueryLabelsResponse>

  /**
   * Refresh an authentication session.
   */
  public suspend fun refreshSession(): AtpResponse<RefreshSessionResponse>

  /**
   * refresh with provided auth
   */
  public suspend fun refreshSession(auth: AuthInfo): AtpResponse<RefreshSessionResponse>

  /**
   * Register for push notifications with a service
   */
  public suspend fun registerPush(request: RegisterPushRequest): AtpResponse<Unit>

  /**
   * Initiate a user account deletion via email.
   */
  public suspend fun requestAccountDelete(): AtpResponse<Unit>

  /**
   * Request a service to persistently crawl hosted repos.
   */
  public suspend fun requestCrawl(request: RequestCrawlRequest): AtpResponse<Unit>

  /**
   * Request an email with a code to confirm ownership of email
   */
  public suspend fun requestEmailConfirmation(): AtpResponse<Unit>

  /**
   * Request a token in order to update email.
   */
  public suspend fun requestEmailUpdate(): AtpResponse<RequestEmailUpdateResponse>

  /**
   * Initiate a user account password reset via email.
   */
  public suspend fun requestPasswordReset(request: RequestPasswordResetRequest): AtpResponse<Unit>

  /**
   * Reset a user account password using a token.
   */
  public suspend fun resetPassword(request: ResetPasswordRequest): AtpResponse<Unit>

  /**
   * Provides the DID of a repo.
   */
  public suspend fun resolveHandle(params: ResolveHandleQueryParams):
      AtpResponse<ResolveHandleResponse>

  /**
   * Resolve moderation reports by an action.
   */
  public suspend fun resolveModerationReports(request: ResolveModerationReportsRequest):
      AtpResponse<ResolveModerationReportsResponse>

  /**
   * Reverse a moderation action.
   */
  public suspend fun reverseModerationAction(request: ReverseModerationActionRequest):
      AtpResponse<ReverseModerationActionResponse>

  /**
   * Revoke an app-specific password by name.
   */
  public suspend fun revokeAppPassword(request: RevokeAppPasswordRequest): AtpResponse<Unit>

  /**
   * Find actors (profiles) matching search criteria.
   */
  public suspend fun searchActors(params: SearchActorsQueryParams):
      AtpResponse<SearchActorsResponse>

  /**
   * Backend Actors (profile) search, returning only skeleton
   */
  public suspend fun searchActorsSkeleton(params: SearchActorsSkeletonQueryParams):
      AtpResponse<SearchActorsSkeletonResponse>

  /**
   * Find actor suggestions for a search term.
   */
  public suspend fun searchActorsTypeahead(params: SearchActorsTypeaheadQueryParams):
      AtpResponse<SearchActorsTypeaheadResponse>

  /**
   * Find posts matching search criteria
   */
  public suspend fun searchPosts(params: SearchPostsQueryParams): AtpResponse<SearchPostsResponse>

  /**
   * Backend Posts search, returning only skeleton
   */
  public suspend fun searchPostsSkeleton(params: SearchPostsSkeletonQueryParams):
      AtpResponse<SearchPostsSkeletonResponse>

  /**
   * Find repositories based on a search term.
   */
  public suspend fun searchRepos(params: SearchReposQueryParams): AtpResponse<SearchReposResponse>

  /**
   * Send email to a user's primary email address
   */
  public suspend fun sendEmail(request: SendEmailRequest): AtpResponse<SendEmailResponse>

  /**
   * Subscribe to label updates
   */
  public suspend fun subscribeLabels(params: SubscribeLabelsQueryParams):
      Flow<AtpResponse<SubscribeLabelsMessage>>

  /**
   * Subscribe to repo updates
   */
  public suspend fun subscribeRepos(params: SubscribeReposQueryParams):
      Flow<AtpResponse<SubscribeReposMessage>>

  /**
   * Take a moderation action on a repo.
   */
  public suspend fun takeModerationAction(request: TakeModerationActionRequest):
      AtpResponse<TakeModerationActionResponse>

  /**
   * Unmute an actor by did or handle.
   */
  public suspend fun unmuteActor(request: UnmuteActorRequest): AtpResponse<Unit>

  /**
   * Unmute a list of actors.
   */
  public suspend fun unmuteActorList(request: UnmuteActorListRequest): AtpResponse<Unit>

  /**
   * Administrative action to update an account's email
   */
  public suspend fun updateAccountEmail(request: UpdateAccountEmailRequest): AtpResponse<Unit>

  /**
   * Administrative action to update an account's handle
   */
  public suspend fun updateAccountHandle(request: UpdateAccountHandleRequest): AtpResponse<Unit>

  /**
   * Update an account's email.
   */
  public suspend fun updateEmail(request: UpdateEmailRequest): AtpResponse<Unit>

  /**
   * Updates the handle of the account
   */
  public suspend fun updateHandle(request: UpdateHandleRequest): AtpResponse<Unit>

  /**
   * Notify server that the user has seen notifications.
   */
  public suspend fun updateSeen(request: UpdateSeenRequest): AtpResponse<Unit>

  /**
   * Upload a new blob to be added to repo in a later request.
   */
  public suspend fun uploadBlob(request: ByteArray): AtpResponse<UploadBlobResponse>
}
