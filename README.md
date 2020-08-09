## Monumental

Monumental is a traveling application that allows the users to search their neighouring area in order to discover the closest and most popular landmarks and to capture these discoveries by taking a picture of the found landmarks. These pictures will in turn be processed using a landmark detection algorithm to verify the authenticity of the pictures and save the user's discoveries into their traveling history which will be available for review at any time.

### Main theoretical concepts: 
-   Clean Architecture
-   Reactive Programming
-   Architectural Patterns (MVVM)
-   Android Development

### Specification

#### Overview

Monumental is a tourism application implemented for Android created with the purpose of demonstrating the way that different the architectural concepts and
models application design models may be applied in a practice. The goal of this documentation is to go into detail regarding the development process and architecture of the application, showcasing how the above theoretical concepts were used as the guiding principles of the application.
#### Purpose

The goal of this application is to provide a way for users to discover popular
monuments while exploring new cities by being notified when in the neighborhood of
one such landmark. When notified, they should be prompted to capture an image of
the monument, thus building a collection of landmarks found over time.
As such, the application’s main incentive is encouraging its users to freely wander
around and explore the cities they are visiting rather than constantly keeping an eye on
their mobile devices or physical maps attempting to discover all available attractions.
After setting up a session by selecting the range, maximum number and type of
landmarks they are interested in, the application should act as a GPS and sensor-based
compass navigating the user to the closest monument detected.


By only providing a general direction and distance from the beacons, the user is
free to roam around and experience his/her surroundings, being able to rely on the
incoming notifications triggered by arriving close to such a beacon.

#### Definitions

-   Beacon : Geographic location represented by its coordinates in the Geographic coordinate system specified as a pair of latitude and longitude values.
-   Geo-fence : A circular region set up around a beacon with its size given as the radius
starting from the center point i.e. the coordinates of the beacon.

#### Functional Requirements

- The user should be able to create an account with a combination of an email address
    and a password.
- The users should be able to authenticate themselves with their created email and
    password accounts or via third-party providers such as Google or Facebook accounts.
- The user should be able to create exploration sessions after being localized by the
    application and specifying the desired details such as radius of search, the limit imposed on the number of landmarks to be found, as well as the preferred categories.
    After searching for points of interest based on the above criteria, the application
    should present the number of landmarks discovered. The user then should have the
    option to start the session or adjust the search criteria. After starting a session, the
    relevant information should be saved on the cloud and be cached locally on the device by the application such that it will not further rely on internet access for basic
    operations.
- The user may save their active session progress at any time to synchronize their
    locally cached data with the cloud database.
- The user may finalize a session prematurely, to which the application should react
    by pushing the recorded session and landmark information to the cloud database
    and wiping the local cache.


- After logging out of the application, any active session started by the user is suspended, allowing him/her to resume it after relogging.
- The application should provide means for resuming, finishing, and synchronizing
    their active session backed up on the cloud on any logged in device.
- When there is an active session, the application must provide a mechanism for guiding the user to the closest beacon set up by showing the general direction and distance from it.
- When there is an active session and the user’s device enters the area of a geo-fence
    set up around one of the session’s beacons, the application should notify the user and
    prompt him/her to take a photo of the landmark represented by the beacon. This
    should be performed via a system notification automatically navigating the user to
    the appropriate screen on opening the notification.
- The application should provide a mechanism for capturing an image via the system
    camera as well as an option for the users to save their images locally in the system’s
    shared directory.
- After capturing an image, the application should be able to process, recognize, and
    label the landmark, prompting the user to repeat the operation with a new image
    if the recognition process fails to identify the given landmark. If the recognition
    process succeeds, the application should cache the image and the time of discovery
    locally.
- After landmark discovery, the application should enqueue the upload process of the
    respective image to the cloud storage.
- The users should be able to view their finished sessions and their discovered landmarks along with their respective images on any logged in device.

#### Non-functional Requirements


**Usability requirements**

- The application should provide clear indications related to its usage and understandable error messages in the case of unexpected faults.


- The application should be built with support for system enabled accessibility
    features such as screen readers.
- The application should follow a consistent design language in terms of fonts,
    text size, imagery, and color scheme.
- The user interface should provide visual notifications in the case of long-running
    but required background tasks and not allow the user to interfere with them.

**Reliability requirements**

- The application should support multiple independent user sessions on the same
    device without them influencing each other.
- The application shall track the device location and send notifications only for
    the currently logged in user with an active session created.
- Locally stored data should be consistent across possibly multiple devices with
    the data persisted on the cloud database.
- The application should be able to handle environmental changes such as changes
    in the screen’s orientation, low device battery, or system-wide preference changes.

**Performance requirements**

- Once a session has been created, the user should be able to resume it without
    any active network connectivity. The navigation mechanisms should also be
    functional with GPS access alone.
- During active session navigation, the application should receive frequent location updates in order to provide the most accurate directions possible.
- The navigation mechanisms relying on device sensor readings such as accelerometers or geomagnetic field sensors should work consistently from any point of
    the globe without being decalibrated by the geomagnetic fields.
- Geo-fence trigger events should be triggered in at most 20 seconds from entering the specified radius.
- The application should use minimal network traffic, using local storage and
    caching mechanisms wherever possible.
- The user interface should never be blocked due to long-running operations such
    as network requests or database read/write actions, but provide visual cues of
    their presence.

**Supportability requirements**

- Independent features of the system should be modifiable with minimal changes
    to the code base and without interfering with unrelated ones.
- The application should provide a concise and general way of implementing
    new features.
- The application should be usable in a similar manner on any specific phone or
    tablet model running on an Android API level of at least 24.
- The application should record and track occurring errors and collect only the
    data relevant to the context of the fault and only from its own perspective.
    
    
#### Use cases

The application’s use cases may be best presented by grouping them into three
distinct categories based on the objects impacted by these interactions and the areas of
the application in which they are to be found. As such, these categories include:

- Use cases related to user authentication and registration, as illustrated in Figure
    4.1.
- Use cases describing the application’s session management (Figure 4.2).
- Use cases describing possible interactions with individual session landmarks, depicted in Figure 4.3.

**4.1** Authentication  | **4.2** Session | **4.3** Landmark 
:------------:|:---------------:|:----------------:
![Imgur](https://i.imgur.com/ws66BZQ.png)  |  ![Imgur](https://i.imgur.com/JeRKJwB.png) | ![Imgur](https://i.imgur.com/sys0tek.png)





### Analysis

![Imgur](https://i.imgur.com/TZLDMut.png)
```Figure 4.5 Sequence diagram: Finishing a user session```

![Imgur](https://i.imgur.com/pxmihjC.png)
```Figure 4.5 Sequence diagram: Landmark detection process```

The above interactions depicted in Figure 4.5 and Figure 4.4 illustrate how the
individual components of an application with a clear separation of concerns primarily
based on Clean Architecture interact with one another. In the subject application, the
use cases and their related business logic is encapsulated into separate objects, whose
lifetime is strictly linked to the time in which they perform the connection between the
boundary objects and the control objects that implement the actual operations defined
in the use case, with the possibility of dynamically chaining or substituting such operations in the case of more complex interactions, such as for the **SaveSessionProgress**
use case.

### Design

Having previously defined the analysis model, this section will focus on presenting the application’s system design model, with an added emphasis on its subsystem decomposition. As it is illustrated in Figure 4.6, the application is structured into
three main subsystems – or modules in this context – as well as with an additional
subsystem in the form of the **FoursquareApi** , encapsulating the networking implementation and subsequent data extraction details of retrieving nearby landmark data
from the external Foursquare REST API service. As such, in the following parts of this
thesis, this module will not be taken into consideration regarding the core architecture
since it is not of central importance in that context.

![Imgur](https://i.imgur.com/rarBRSO.png)

Based on the previous statement and according to the guiding architectural model, the three core modules of the application are considered to be the following:

- **Domain** : encapsulating the central entity objects and the use case objects describing the possible scenarios of interaction based on the business rules defined during specification. This being the highest level module of the application’s architecture, it is a completely platform independent Java module, providing greaterreusability and portability to other JVM powered environments.


- **Data** : providing implementations in the form of interface adapters for the operations defined in the domain layer. This module is primarily responsible for
    facilitating the local data management of the system and its interaction with external agencies such as cloud services, providers, and data storage. Furthermore,
    it defines entity objects and related mappers local to this module which are of
    greater convenience when interacting with the specific frameworks and libraries
    employed.
- **Presentation** : exclusively being the module containing the boundary objects found
    during analysis. This is the place where both the **View** and **ViewModel** components of the systems MVVM architecture are implemented, defining the user
    interface along with all the presentation logic. This being the lowest level subsystem, additional platform-specific components are implemented here such as
    those responsible for managing the geofences, these relying heavily on Android’s
    architecture components.
The way these aforementioned modules depend on one another is perfectly in
line with the principles of Clean Architecture, with the domain module being completely unaware of any other subsystem, and the modules above it in the hierarchy
each relying only on components residing beneath them. Furthermore, Figure 4.7 illustrates the control objects discerned during analysis and their subsequent implementations in lower level modules.


![Imgur](https://i.imgur.com/sGedgz5.png)
```
Figure 4.7: Component diagram of the interface adapters
```

### Implementation

![Imgur](https://i.imgur.com/Blld1Bh.png)
```
Figure 4.8: Simplified class diagram
```

#### Technologies & Libraries

Regarding the technologies and libraries used during implementation the following key ones may be evidentiated:

- **Firebase** : A number of services offered within the Firebase development platform have been used given the level of abstraction provided by them on top
    of the implementation details of the application’s cloud interaction needs. The
    respective services employed: **Authorization** : for managing user authentication
    and data management, **Firestore** : for persisting session data online within a well-optimized NoSQL database, **Storage** : for backing up landmark images captured
    by the users within their discovery sessions, and **MLKit** : for making use of the
    large volume of images and metadata available on the cloud for image labeling
    and landmark detection.
- **RxJava & RxAndroid** : Reactive Extensions provide the primary mechanism for
    facilitating the interaction between components of the system, having all operations of the interface adapters communicate via reactive sources.
- **Lifecycle-Aware components and ViewModel** : For the implementation of the
    presentation layer in a lifecycle-conscious manner, making sure observable stream
    subscriptions and ViewModel presence are managed without being impacted by
    the changes in the state of Android’s UI components.
- **Retrofit** : Providing a concise interface for creating and managing REST API requests with the possibility of wrapping them into observable sources.
- **Room Persistence Library** : For the implementation of the local session cache,
    providing a level of abstraction on top of Android’s SQLite system database.
- **WorkManager** : For enqueueing and executing long-running background tasks
    which may impose certain constraints on their execution such as network access
    or storage availability.
- **Navigation Component** : For implementing the Single Activity Model essentially defining the flow of the application’s UI navigation in a concise and clear manner, having it described as a collection of destinations and possible navigation actions between said destinations.
    
    
Authentication |  Home | Session setup 
:-------------:|:----------------:|:----------------:
![Imgur](https://i.imgur.com/6fdEIAU.jpg)  |  ![Imgur](https://i.imgur.com/ok1C3cb.jpg) | ![Imgur](https://i.imgur.com/eMzDEpy.jpg)

Landmark categories |  Navigation | Landmark detection
:-------------:|:----------------:|:----------------:
![Imgur](https://i.imgur.com/CUAqHYr.jpg)  |  ![Imgur](https://i.imgur.com/URcHoDJ.jpg) | ![Imgur](https://i.imgur.com/PkY6kom.jpg)
