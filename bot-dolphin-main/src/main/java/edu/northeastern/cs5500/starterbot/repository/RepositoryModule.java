package edu.northeastern.cs5500.starterbot.repository;

import dagger.Module;
import dagger.Provides;
import edu.northeastern.cs5500.starterbot.ExcludeFromJacocoGeneratedReport;
import edu.northeastern.cs5500.starterbot.model.EventUser;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;

@ExcludeFromJacocoGeneratedReport
@Module
public class RepositoryModule {
    @Provides
    public GenericRepository<EventUser> provideEventUsersRepository(
            MongoDBRepository<EventUser> repository) {
        return repository;
    }

    @Provides
    public Class<EventUser> provideEventUser() {
        return EventUser.class;
    }

    @Provides
    public GenericRepository<StudyEvent> provideStudyEventsRepository(
            MongoDBRepository<StudyEvent> repository) {
        return repository;
    }

    @Provides
    public Class<StudyEvent> provideStudyEvent() {
        return StudyEvent.class;
    }
}
