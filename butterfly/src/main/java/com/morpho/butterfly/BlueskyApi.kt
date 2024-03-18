package com.morpho.butterfly

import app.bsky.actor.GetPreferencesResponse
import app.bsky.actor.GetProfileQueryParams
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
import com.atproto.admin.GetModerationActionsQueryParams
import com.atproto.admin.GetModerationActionsResponse
import com.atproto.admin.GetModerationReportQueryParams
import com.atproto.admin.GetModerationReportsQueryParams
import com.atproto.admin.GetModerationReportsResponse
import com.atproto.admin.ResolveModerationReportsRequest
import com.atproto.admin.ReverseModerationActionRequest
import com.atproto.admin.SearchReposQueryParams
import com.atproto.admin.SearchReposResponse
import com.atproto.admin.SendEmailRequest
import com.atproto.admin.SendEmailResponse
import com.atproto.admin.TakeModerationActionRequest
import com.atproto.admin.UpdateAccountEmailRequest
import com.atproto.admin.UpdateAccountHandleRequest
import com.atproto.identity.ResolveHandleQueryParams
import com.atproto.identity.ResolveHandleResponse
import com.atproto.identity.UpdateHandleRequest
import com.atproto.label.QueryLabelsQueryParams
import com.atproto.label.QueryLabelsResponse
import com.atproto.label.SubscribeLabelsQueryParams
import com.atproto.moderation.CreateReportRequest
import com.atproto.moderation.CreateReportResponse
import com.atproto.repo.ApplyWritesRequest
import com.atproto.repo.CreateRecordRequest
import com.atproto.repo.CreateRecordResponse
import com.atproto.repo.DeleteRecordRequest
import com.atproto.repo.DescribeRepoQueryParams
import com.atproto.repo.DescribeRepoResponse
import com.atproto.repo.GetRecordQueryParams
import com.atproto.repo.GetRecordResponse
import com.atproto.repo.ListRecordsQueryParams
import com.atproto.repo.ListRecordsResponse
import com.atproto.repo.PutRecordRequest
import com.atproto.repo.PutRecordResponse
import com.atproto.repo.UploadBlobResponse
import com.atproto.server.ConfirmEmailRequest
import com.atproto.server.CreateAccountRequest
import com.atproto.server.CreateAccountResponse
import com.atproto.server.CreateAppPasswordRequest
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
import com.atproto.sync.GetBlocksQueryParams
import com.atproto.sync.GetCheckoutQueryParams
import com.atproto.sync.GetHeadQueryParams
import com.atproto.sync.GetHeadResponse
import com.atproto.sync.GetLatestCommitQueryParams
import com.atproto.sync.GetLatestCommitResponse
import com.atproto.sync.GetRepoQueryParams
import com.atproto.sync.ListBlobsQueryParams
import com.atproto.sync.ListBlobsResponse
import com.atproto.sync.ListReposQueryParams
import com.atproto.sync.ListReposResponse
import com.atproto.sync.NotifyOfUpdateRequest
import com.atproto.sync.RequestCrawlRequest
import com.atproto.sync.SubscribeReposQueryParams
import com.morpho.butterfly.auth.TokenInfo
import kotlinx.coroutines.flow.Flow

public interface BlueskyApi {
    /**
     * Allow a labeler to apply labels directly.
     */
    public suspend fun applyLabels(request: ApplyLabelsRequest): Result<Unit>

    /**
     * Apply a batch transaction of creates, updates, and deletes.
     */
    public suspend fun applyWrites(request: ApplyWritesRequest): Result<Unit>

    /**
     * Confirm an email using a token from com.atproto.server.requestEmailConfirmation.
     */
    public suspend fun confirmEmail(request: ConfirmEmailRequest): Result<Unit>

    /**
     * Create an account.
     */
    public suspend fun createAccount(request: CreateAccountRequest):
            Result<CreateAccountResponse>

    /**
     * Create an app-specific password.
     */
    public suspend fun createAppPassword(request: CreateAppPasswordRequest):
            Result<CreateAppPasswordResponse>

    /**
     * Create an invite code.
     */
    public suspend fun createInviteCode(request: CreateInviteCodeRequest):
            Result<CreateInviteCodeResponse>

    /**
     * Create an invite code.
     */
    public suspend fun createInviteCodes(request: CreateInviteCodesRequest):
            Result<CreateInviteCodesResponse>

    /**
     * Create a new record.
     */
    public suspend fun createRecord(request: CreateRecordRequest): Result<CreateRecordResponse>

    /**
     * Report a repo or a record.
     */
    public suspend fun createReport(request: CreateReportRequest): Result<CreateReportResponse>

    /**
     * Create an authentication session.
     */
    public suspend fun createSession(request: CreateSessionRequest):
            Result<CreateSessionResponse>

    /**
     * Delete a user account with a token and password.
     */
    public suspend fun deleteAccount(request: DeleteAccountRequest): Result<Unit>

    /**
     * Delete a record, or ensure it doesn't exist.
     */
    public suspend fun deleteRecord(request: DeleteRecordRequest): Result<Unit>

    /**
     * Delete the current session.
     */
    public suspend fun deleteSession(): Result<Unit>

    /**
     * Returns information about a given feed generator including TOS & offered feed URIs
     */
    public suspend fun describeFeedGenerator(): Result<DescribeFeedGeneratorResponse>

    /**
     * Get information about the repo, including the list of collections.
     */
    public suspend fun describeRepo(params: DescribeRepoQueryParams):
            Result<DescribeRepoResponse>

    /**
     * Get a document describing the service's accounts configuration.
     */
    public suspend fun describeServer(): Result<DescribeServerResponse>

    /**
     * Disable an account from receiving new invite codes, but does not invalidate existing codes
     */
    public suspend fun disableAccountInvites(request: DisableAccountInvitesRequest): Result<Unit>

    /**
     * Disable some set of codes and/or all codes associated with a set of users
     */
    public suspend fun disableInviteCodes(request: DisableInviteCodesRequest): Result<Unit>

    /**
     * Re-enable an accounts ability to receive invite codes
     */
    public suspend fun enableAccountInvites(request: EnableAccountInvitesRequest): Result<Unit>

    /**
     * Get all invite codes for a given account
     */
    public suspend fun getAccountInviteCodes(params: GetAccountInviteCodesQueryParams):
            Result<GetAccountInviteCodesResponse>

    /**
     * Retrieve a list of feeds created by a given actor
     */
    public suspend fun getActorFeeds(params: GetActorFeedsQueryParams):
            Result<GetActorFeedsResponse>

    /**
     * A view of the posts liked by an actor.
     */
    public suspend fun getActorLikes(params: GetActorLikesQueryParams):
            Result<GetActorLikesResponse>

    /**
     * A view of an actor's feed.
     */
    public suspend fun getAuthorFeed(params: GetAuthorFeedQueryParams):
            Result<GetAuthorFeedResponse>

    /**
     * Get a blob associated with a given repo.
     */
    public suspend fun getBlob(params: GetBlobQueryParams): Result<ByteArray>

    /**
     * Gets blocks from a given repo.
     */
    public suspend fun getBlocks(params: GetBlocksQueryParams): Result<ByteArray>

    /**
     * Who is the requester's account blocking?
     */
    public suspend fun getBlocks(params: app.bsky.graph.GetBlocksQueryParams): Result<GetBlocksResponse>

    /**
     * DEPRECATED - please use com.atproto.sync.getRepo instead
     */
    public suspend fun getCheckout(params: GetCheckoutQueryParams): Result<ByteArray>

    /**
     * Compose and hydrate a feed from a user's selected feed generator
     */
    public suspend fun getFeed(params: GetFeedQueryParams): Result<GetFeedResponse>

    /**
     * Get information about a specific feed offered by a feed generator, such as its online status
     */
    public suspend fun getFeedGenerator(params: GetFeedGeneratorQueryParams):
            Result<GetFeedGeneratorResponse>

    /**
     * Get information about a list of feed generators
     */
    public suspend fun getFeedGenerators(params: GetFeedGeneratorsQueryParams):
            Result<GetFeedGeneratorsResponse>

    /**
     * A skeleton of a feed provided by a feed generator
     */
    public suspend fun getFeedSkeleton(params: GetFeedSkeletonQueryParams):
            Result<GetFeedSkeletonResponse>

    /**
     * Who is following an actor?
     */
    public suspend fun getFollowers(params: GetFollowersQueryParams):
            Result<GetFollowersResponse>

    /**
     * Who is an actor following?
     */
    public suspend fun getFollows(params: GetFollowsQueryParams): Result<GetFollowsResponse>

    /**
     * DEPRECATED - please use com.atproto.sync.getLatestCommit instead
     */
    public suspend fun getHead(params: GetHeadQueryParams): Result<GetHeadResponse>

    /**
     * Admin view of invite codes
     */
    public suspend fun getInviteCodes(params: GetInviteCodesQueryParams):
            Result<GetInviteCodesResponse>

    /**
     * Gets the current commit CID & revision of the repo.
     */
    public suspend fun getLatestCommit(params: GetLatestCommitQueryParams):
            Result<GetLatestCommitResponse>

    public suspend fun getLikes(params: GetLikesQueryParams): Result<GetLikesResponse>

    /**
     * Fetch a list of actors
     */
    public suspend fun getList(params: GetListQueryParams): Result<GetListResponse>

    /**
     * Which lists is the requester's account blocking?
     */
    public suspend fun getListBlocks(params: GetListBlocksQueryParams):
            Result<GetListBlocksResponse>

    /**
     * A view of a recent posts from actors in a list
     */
    public suspend fun getListFeed(params: GetListFeedQueryParams): Result<GetListFeedResponse>

    /**
     * Which lists is the requester's account muting?
     */
    public suspend fun getListMutes(params: GetListMutesQueryParams):
            Result<GetListMutesResponse>

    /**
     * Fetch a list of lists that belong to an actor
     */
    public suspend fun getLists(params: GetListsQueryParams): Result<GetListsResponse>

    /**
     * View details about a moderation action.
     */
    public suspend fun getModerationAction(params: GetModerationActionQueryParams):
            Result<GetModerationActionResponse>

    /**
     * List moderation actions related to a subject.
     */
    public suspend fun getModerationActions(params: GetModerationActionsQueryParams):
            Result<GetModerationActionsResponse>

    /**
     * View details about a moderation report.
     */
    public suspend fun getModerationReport(params: GetModerationReportQueryParams):
            Result<GetModerationReportResponse>

    /**
     * List moderation reports related to a subject.
     */
    public suspend fun getModerationReports(params: GetModerationReportsQueryParams):
            Result<GetModerationReportsResponse>

    /**
     * Who does the viewer mute?
     */
    public suspend fun getMutes(params: GetMutesQueryParams): Result<GetMutesResponse>

    /**
     * DEPRECATED: will be removed soon, please find a feed generator alternative
     */
    public suspend fun getPopular(params: GetPopularQueryParams): Result<GetPopularResponse>

    /**
     * An unspecced view of globally popular feed generators
     */
    public suspend fun getPopularFeedGenerators(params: GetPopularFeedGeneratorsQueryParams):
            Result<GetPopularFeedGeneratorsResponse>

    public suspend fun getPostThread(params: GetPostThreadQueryParams):
            Result<GetPostThreadResponse>

    /**
     * A view of an actor's feed.
     */
    public suspend fun getPosts(params: GetPostsQueryParams): Result<GetPostsResponse>

    /**
     * Get private preferences attached to the account.
     */
    public suspend fun getPreferences(): Result<GetPreferencesResponse>

    public suspend fun getProfile(params: GetProfileQueryParams): Result<GetProfileResponse>

    public suspend fun getProfiles(params: GetProfilesQueryParams): Result<GetProfilesResponse>

    /**
     * Get a record.
     */
    public suspend fun getRecord(params: GetRecordQueryParams): Result<GetRecordResponse>

    /**
     * Gets blocks needed for existence or non-existence of record.
     */
    public suspend fun getRecord(params: com.atproto.sync.GetRecordQueryParams): Result<ByteArray>

    /**
     * View details about a record.
     */
    public suspend fun getRecord(params: com.atproto.admin.GetRecordQueryParams):
            Result<AdminGetRecordResponse>

    /**
     * Gets the did's repo, optionally catching up from a specific revision.
     */
    public suspend fun getRepo(params: GetRepoQueryParams): Result<ByteArray>

    /**
     * View details about a repository.
     */
    public suspend fun getRepo(params: com.atproto.admin.GetRepoQueryParams): Result<GetRepoResponse>

    public suspend fun getRepostedBy(params: GetRepostedByQueryParams):
            Result<GetRepostedByResponse>

    /**
     * Get information about the current session.
     */
    public suspend fun getSession(): Result<GetSessionResponse>

    /**
     * Get a list of suggested feeds for the viewer.
     */
    public suspend fun getSuggestedFeeds(params: GetSuggestedFeedsQueryParams):
            Result<GetSuggestedFeedsResponse>

    /**
     * Get suggested follows related to a given actor.
     */
    public suspend fun getSuggestedFollowsByActor(params: GetSuggestedFollowsByActorQueryParams):
            Result<GetSuggestedFollowsByActorResponse>

    /**
     * Get a list of actors suggested for following. Used in discovery UIs.
     */
    public suspend fun getSuggestions(params: GetSuggestionsQueryParams):
            Result<GetSuggestionsResponse>

    /**
     * A view of the user's home timeline.
     */
    public suspend fun getTimeline(params: GetTimelineQueryParams): Result<GetTimelineResponse>

    /**
     * A skeleton of a timeline - UNSPECCED & WILL GO AWAY SOON
     */
    public suspend fun getTimelineSkeleton(params: GetTimelineSkeletonQueryParams):
            Result<GetTimelineSkeletonResponse>

    public suspend fun getUnreadCount(params: GetUnreadCountQueryParams):
            Result<GetUnreadCountResponse>

    /**
     * List all app-specific passwords.
     */
    public suspend fun listAppPasswords(): Result<ListAppPasswordsResponse>

    /**
     * List blob cids since some revision
     */
    public suspend fun listBlobs(params: ListBlobsQueryParams): Result<ListBlobsResponse>

    public suspend fun listNotifications(params: ListNotificationsQueryParams):
            Result<ListNotificationsResponse>

    /**
     * List a range of records in a collection.
     */
    public suspend fun listRecords(params: ListRecordsQueryParams): Result<ListRecordsResponse>

    /**
     * List dids and root cids of hosted repos
     */
    public suspend fun listRepos(params: ListReposQueryParams): Result<ListReposResponse>

    /**
     * Mute an actor by did or handle.
     */
    public suspend fun muteActor(request: MuteActorRequest): Result<Unit>

    /**
     * Mute a list of actors.
     */
    public suspend fun muteActorList(request: MuteActorListRequest): Result<Unit>

    /**
     * Notify a crawling service of a recent update. Often when a long break between updates causes
     * the connection with the crawling service to break.
     */
    public suspend fun notifyOfUpdate(request: NotifyOfUpdateRequest): Result<Unit>

    /**
     * Sets the private preferences attached to the account.
     */
    public suspend fun putPreferences(request: PutPreferencesRequest): Result<Unit>

    /**
     * Write a record, creating or updating it as needed.
     */
    public suspend fun putRecord(request: PutRecordRequest): Result<PutRecordResponse>

    /**
     * Find labels relevant to the provided URI patterns.
     */
    public suspend fun queryLabels(params: QueryLabelsQueryParams): Result<QueryLabelsResponse>

    /**
     * Refresh an authentication session.
     */
    public suspend fun refreshSession(): Result<RefreshSessionResponse>

    /**
     * refresh with provided auth
     */
    public suspend fun refreshSession(auth: TokenInfo): Result<RefreshSessionResponse>

    /**
     * Register for push notifications with a service
     */
    public suspend fun registerPush(request: RegisterPushRequest): Result<Unit>

    /**
     * Initiate a user account deletion via email.
     */
    public suspend fun requestAccountDelete(): Result<Unit>

    /**
     * Request a service to persistently crawl hosted repos.
     */
    public suspend fun requestCrawl(request: RequestCrawlRequest): Result<Unit>

    /**
     * Request an email with a code to confirm ownership of email
     */
    public suspend fun requestEmailConfirmation(): Result<Unit>

    /**
     * Request a token in order to update email.
     */
    public suspend fun requestEmailUpdate(): Result<RequestEmailUpdateResponse>

    /**
     * Initiate a user account password reset via email.
     */
    public suspend fun requestPasswordReset(request: RequestPasswordResetRequest): Result<Unit>

    /**
     * Reset a user account password using a token.
     */
    public suspend fun resetPassword(request: ResetPasswordRequest): Result<Unit>

    /**
     * Provides the DID of a repo.
     */
    public suspend fun resolveHandle(params: ResolveHandleQueryParams):
            Result<ResolveHandleResponse>

    /**
     * Resolve moderation reports by an action.
     */
    public suspend fun resolveModerationReports(request: ResolveModerationReportsRequest):
            Result<ResolveModerationReportsResponse>

    /**
     * Reverse a moderation action.
     */
    public suspend fun reverseModerationAction(request: ReverseModerationActionRequest):
            Result<ReverseModerationActionResponse>

    /**
     * Revoke an app-specific password by name.
     */
    public suspend fun revokeAppPassword(request: RevokeAppPasswordRequest): Result<Unit>

    /**
     * Find actors (profiles) matching search criteria.
     */
    public suspend fun searchActors(params: SearchActorsQueryParams):
            Result<SearchActorsResponse>

    /**
     * Backend Actors (profile) search, returning only skeleton
     */
    public suspend fun searchActorsSkeleton(params: SearchActorsSkeletonQueryParams):
            Result<SearchActorsSkeletonResponse>

    /**
     * Find actor suggestions for a search term.
     */
    public suspend fun searchActorsTypeahead(params: SearchActorsTypeaheadQueryParams):
            Result<SearchActorsTypeaheadResponse>

    /**
     * Find posts matching search criteria
     */
    public suspend fun searchPosts(params: SearchPostsQueryParams): Result<SearchPostsResponse>

    /**
     * Backend Posts search, returning only skeleton
     */
    public suspend fun searchPostsSkeleton(params: SearchPostsSkeletonQueryParams):
            Result<SearchPostsSkeletonResponse>

    /**
     * Find repositories based on a search term.
     */
    public suspend fun searchRepos(params: SearchReposQueryParams): Result<SearchReposResponse>

    /**
     * Send email to a user's primary email address
     */
    public suspend fun sendEmail(request: SendEmailRequest): Result<SendEmailResponse>

    /**
     * Subscribe to label updates
     */
    public suspend fun subscribeLabels(params: SubscribeLabelsQueryParams):
            Flow<Result<SubscribeLabelsMessage>>

    /**
     * Subscribe to repo updates
     */
    public suspend fun subscribeRepos(params: SubscribeReposQueryParams):
            Flow<Result<SubscribeReposMessage>>

    /**
     * Take a moderation action on a repo.
     */
    public suspend fun takeModerationAction(request: TakeModerationActionRequest):
            Result<TakeModerationActionResponse>

    /**
     * Unmute an actor by did or handle.
     */
    public suspend fun unmuteActor(request: UnmuteActorRequest): Result<Unit>

    /**
     * Unmute a list of actors.
     */
    public suspend fun unmuteActorList(request: UnmuteActorListRequest): Result<Unit>

    /**
     * Administrative action to update an account's email
     */
    public suspend fun updateAccountEmail(request: UpdateAccountEmailRequest): Result<Unit>

    /**
     * Administrative action to update an account's handle
     */
    public suspend fun updateAccountHandle(request: UpdateAccountHandleRequest): Result<Unit>

    /**
     * Update an account's email.
     */
    public suspend fun updateEmail(request: UpdateEmailRequest): Result<Unit>

    /**
     * Updates the handle of the account
     */
    public suspend fun updateHandle(request: UpdateHandleRequest): Result<Unit>

    /**
     * Notify server that the user has seen notifications.
     */
    public suspend fun updateSeen(request: UpdateSeenRequest): Result<Unit>

    /**
     * Upload a new blob to be added to repo in a later request.
     */
    public suspend fun uploadBlob(request: ByteArray): Result<UploadBlobResponse>
}
