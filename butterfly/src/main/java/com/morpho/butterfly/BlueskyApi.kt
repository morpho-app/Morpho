package com.morpho.butterfly

import app.bsky.actor.*
import app.bsky.feed.*
import app.bsky.graph.*
import app.bsky.notification.*
import app.bsky.unspecced.*
import com.atproto.admin.*
import com.atproto.identity.*
import com.atproto.label.*
import com.atproto.moderation.*
import com.atproto.repo.*
import com.atproto.repo.GetRecordQuery
import com.atproto.repo.GetRecordResponse
import com.atproto.server.*
import com.atproto.sync.*
import com.atproto.sync.GetBlocksQuery
import com.atproto.sync.GetRepoQuery

import com.morpho.butterfly.auth.AuthInfo
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
    public suspend fun describeRepo(params: DescribeRepoQuery):
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
    public suspend fun getAccountInviteCodes(params: GetAccountInviteCodesQuery):
            Result<GetAccountInviteCodesResponse>

    /**
     * Retrieve a list of feeds created by a given actor
     */
    public suspend fun getActorFeeds(params: GetActorFeedsQuery):
            Result<GetActorFeedsResponse>

    /**
     * A view of the posts liked by an actor.
     */
    public suspend fun getActorLikes(params: GetActorLikesQuery):
            Result<GetActorLikesResponse>

    /**
     * A view of an actor's feed.
     */
    public suspend fun getAuthorFeed(params: GetAuthorFeedQuery):
            Result<GetAuthorFeedResponse>

    /**
     * Get a blob associated with a given repo.
     */
    public suspend fun getBlob(params: GetBlobQuery): Result<ByteArray>

    /**
     * Gets blocks from a given repo.
     */
    public suspend fun getBlocks(params: GetBlocksQuery): Result<ByteArray>

    /**
     * Who is the requester's account blocking?
     */
    public suspend fun getBlocks(params: app.bsky.graph.GetBlocksQuery): Result<GetBlocksResponse>

    /**
     * DEPRECATED - please use com.atproto.sync.getRepo instead
     */
    public suspend fun getCheckout(params: GetCheckoutQuery): Result<ByteArray>

    /**
     * Compose and hydrate a feed from a user's selected feed generator
     */
    public suspend fun getFeed(params: GetFeedQuery): Result<GetFeedResponse>

    /**
     * Get information about a specific feed offered by a feed generator, such as its online status
     */
    public suspend fun getFeedGenerator(params: GetFeedGeneratorQuery):
            Result<GetFeedGeneratorResponse>

    /**
     * Get information about a list of feed generators
     */
    public suspend fun getFeedGenerators(params: GetFeedGeneratorsQuery):
            Result<GetFeedGeneratorsResponse>

    /**
     * A skeleton of a feed provided by a feed generator
     */
    public suspend fun getFeedSkeleton(params: GetFeedSkeletonQuery):
            Result<GetFeedSkeletonResponse>

    /**
     * Who is following an actor?
     */
    public suspend fun getFollowers(params: GetFollowersQuery):
            Result<GetFollowersResponse>

    /**
     * Who is an actor following?
     */
    public suspend fun getFollows(params: GetFollowsQuery): Result<GetFollowsResponse>

    /**
     * DEPRECATED - please use com.atproto.sync.getLatestCommit instead
     */
    public suspend fun getHead(params: GetHeadQuery): Result<GetHeadResponse>

    /**
     * Admin view of invite codes
     */
    public suspend fun getInviteCodes(params: GetInviteCodesQuery):
            Result<GetInviteCodesResponse>

    /**
     * Gets the current commit CID & revision of the repo.
     */
    public suspend fun getLatestCommit(params: GetLatestCommitQuery):
            Result<GetLatestCommitResponse>

    public suspend fun getLikes(params: GetLikesQuery): Result<GetLikesResponse>

    /**
     * Fetch a list of actors
     */
    public suspend fun getList(params: GetListQuery): Result<GetListResponse>

    /**
     * Which lists is the requester's account blocking?
     */
    public suspend fun getListBlocks(params: GetListBlocksQuery):
            Result<GetListBlocksResponse>

    /**
     * A view of a recent posts from actors in a list
     */
    public suspend fun getListFeed(params: GetListFeedQuery): Result<GetListFeedResponse>

    /**
     * Which lists is the requester's account muting?
     */
    public suspend fun getListMutes(params: GetListMutesQuery):
            Result<GetListMutesResponse>

    /**
     * Fetch a list of lists that belong to an actor
     */
    public suspend fun getLists(params: GetListsQuery): Result<GetListsResponse>

    /**
     * View details about a moderation action.
     */
    public suspend fun getModerationAction(params: GetModerationActionQuery):
            Result<GetModerationActionResponse>

    /**
     * List moderation actions related to a subject.
     */
    public suspend fun getModerationActions(params: GetModerationActionsQuery):
            Result<GetModerationActionsResponse>

    /**
     * View details about a moderation report.
     */
    public suspend fun getModerationReport(params: GetModerationReportQuery):
            Result<GetModerationReportResponse>

    /**
     * List moderation reports related to a subject.
     */
    public suspend fun getModerationReports(params: GetModerationReportsQuery):
            Result<GetModerationReportsResponse>

    /**
     * Who does the viewer mute?
     */
    public suspend fun getMutes(params: GetMutesQuery): Result<GetMutesResponse>

    /**
     * DEPRECATED: will be removed soon, please find a feed generator alternative
     */
    public suspend fun getPopular(params: GetPopularQuery): Result<GetPopularResponse>

    /**
     * An unspecced view of globally popular feed generators
     */
    public suspend fun getPopularFeedGenerators(params: GetPopularFeedGeneratorsQuery):
            Result<GetPopularFeedGeneratorsResponse>

    public suspend fun getPostThread(params: GetPostThreadQuery):
            Result<GetPostThreadResponse>

    /**
     * A view of an actor's feed.
     */
    public suspend fun getPosts(params: GetPostsQuery): Result<GetPostsResponse>

    /**
     * Get private preferences attached to the account.
     */
    public suspend fun getPreferences(): Result<GetPreferencesResponse>

    public suspend fun getProfile(params: GetProfileQuery): Result<GetProfileResponse>

    public suspend fun getProfiles(params: GetProfilesQuery): Result<GetProfilesResponse>

    /**
     * Get a record.
     */
    public suspend fun getRecord(params: GetRecordQuery): Result<GetRecordResponse>

    /**
     * Gets blocks needed for existence or non-existence of record.
     */
    public suspend fun getRecord(params: com.atproto.sync.GetRecordQuery): Result<ByteArray>

    /**
     * View details about a record.
     */
    public suspend fun getRecord(params: com.atproto.admin.GetRecordQuery):
            Result<GetRecordResponse>

    /**
     * Gets the did's repo, optionally catching up from a specific revision.
     */
    public suspend fun getRepo(params: GetRepoQuery): Result<ByteArray>

    /**
     * View details about a repository.
     */
    public suspend fun getRepo(params: com.atproto.admin.GetRepoQuery): Result<GetRepoResponse>

    public suspend fun getRepostedBy(params: GetRepostedByQuery):
            Result<GetRepostedByResponse>

    /**
     * Get information about the current session.
     */
    public suspend fun getSession(): Result<GetSessionResponse>

    /**
     * Get a list of suggested feeds for the viewer.
     */
    public suspend fun getSuggestedFeeds(params: GetSuggestedFeedsQuery):
            Result<GetSuggestedFeedsResponse>

    /**
     * Get suggested follows related to a given actor.
     */
    public suspend fun getSuggestedFollowsByActor(params: GetSuggestedFollowsByActorQuery):
            Result<GetSuggestedFollowsByActorResponse>

    /**
     * Get a list of actors suggested for following. Used in discovery UIs.
     */
    public suspend fun getSuggestions(params: GetSuggestionsQuery):
            Result<GetSuggestionsResponse>

    /**
     * A view of the user's home timeline.
     */
    public suspend fun getTimeline(params: GetTimelineQuery): Result<GetTimelineResponse>

    /**
     * A skeleton of a timeline - UNSPECCED & WILL GO AWAY SOON
     */
    public suspend fun getTimelineSkeleton(params: GetTimelineSkeletonQuery):
            Result<GetTimelineSkeletonResponse>

    public suspend fun getUnreadCount(params: GetUnreadCountQuery):
            Result<GetUnreadCountResponse>

    /**
     * List all app-specific passwords.
     */
    public suspend fun listAppPasswords(): Result<ListAppPasswordsResponse>

    /**
     * List blob cids since some revision
     */
    public suspend fun listBlobs(params: ListBlobsQuery): Result<ListBlobsResponse>

    public suspend fun listNotifications(params: ListNotificationsQuery):
            Result<ListNotificationsResponse>

    /**
     * List a range of records in a collection.
     */
    public suspend fun listRecords(params: ListRecordsQuery): Result<ListRecordsResponse>

    /**
     * List dids and root cids of hosted repos
     */
    public suspend fun listRepos(params: ListReposQuery): Result<ListReposResponse>

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
    public suspend fun queryLabels(params: QueryLabels): Result<QueryLabelsResponse>

    /**
     * Refresh an authentication session.
     */
    public suspend fun refreshSession(): Result<RefreshSessionResponse>

    /**
     * refresh with provided auth
     */
    public suspend fun refreshSession(auth: AuthInfo): Result<RefreshSessionResponse>

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
    public suspend fun resolveHandle(params: ResolveHandleQuery):
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
    public suspend fun searchActors(params: SearchActorsQuery):
            Result<SearchActorsResponse>

    /**
     * Backend Actors (profile) search, returning only skeleton
     */
    public suspend fun searchActorsSkeleton(params: SearchActorsSkeletonQuery):
            Result<SearchActorsSkeletonResponse>

    /**
     * Find actor suggestions for a search term.
     */
    public suspend fun searchActorsTypeahead(params: SearchActorsTypeaheadQuery):
            Result<SearchActorsTypeaheadResponse>

    /**
     * Find posts matching search criteria
     */
    public suspend fun searchPosts(params: SearchPostsQuery): Result<SearchPostsResponse>

    /**
     * Backend Posts search, returning only skeleton
     */
    public suspend fun searchPostsSkeleton(params: SearchPostsSkeletonQuery):
            Result<SearchPostsSkeletonResponse>

    /**
     * Find repositories based on a search term.
     */
    public suspend fun searchRepos(params: SearchReposQuery): Result<SearchReposResponse>

    /**
     * Send email to a user's primary email address
     */
    public suspend fun sendEmail(request: SendEmailRequest): Result<SendEmailResponse>

    /**
     * Subscribe to label updates
     */
    public suspend fun subscribeLabels(params: SubscribeLabelsQuery):
            Flow<Result<SubscribeLabelsMessage>>

    /**
     * Subscribe to repo updates
     */
    public suspend fun subscribeRepos(params: SubscribeReposQuery):
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

