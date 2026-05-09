package com.estatex.application.acceptance.fakes;

import com.estatex.application.chat.ChatService;
import com.estatex.application.favourite.FavouriteService;
import com.estatex.application.listing.ListingService;
import com.estatex.application.user.UserService;

public class TestingBackendSetup {

    public final InMemoryUserRepository userRepository;
    public final InMemoryListingRepository listingRepository;
    public final InMemoryConversationRepository conversationRepository;
    public final InMemoryMessageRepository messageRepository;
    public final InMemoryFavouriteRepository favouriteRepository;
    public final InMemoryFileStoragePort fileStoragePort;

    public final UserService userService;
    public final ListingService listingService;
    public final ChatService chatService;
    public final FavouriteService favouriteService;

    public TestingBackendSetup() {
        this.userRepository = new InMemoryUserRepository();
        this.listingRepository = new InMemoryListingRepository();
        this.conversationRepository = new InMemoryConversationRepository();
        this.messageRepository = new InMemoryMessageRepository();
        this.favouriteRepository = new InMemoryFavouriteRepository();
        this.fileStoragePort = new InMemoryFileStoragePort();

        this.userService = new UserService(this.userRepository);
        this.userRepository.setListingRepository(this.listingRepository);
        this.listingService = new ListingService(this.listingRepository, this.userRepository, this.fileStoragePort);
        this.chatService = new ChatService(
                this.conversationRepository,
                this.messageRepository,
                this.listingRepository,
                this.userRepository,
                this.fileStoragePort
        );
        this.favouriteService = new FavouriteService(this.favouriteRepository, this.listingRepository);
    }
}
