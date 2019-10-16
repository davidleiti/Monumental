# Monumental

Android app written for presenting the practical application of the architectural concepts described in my Bachelor's Thesis.

### 4.1 Specification

#### 4.1.1 Overview

Monumental is a tourism application implemented on the Android operating sys-
tem created with the purpose of demonstrating the way the architectural concepts and
models presented in the previous theoretical chapters may be applied in a practice.
The goal of this chapter is to present the steps that constitute the development process
of the application starting from the original requirements up until the actual imple-
mentation activities and details.

#### 4.1.2 Purpose

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

#### 4.1.3 Definitions

Beacon : Geographic location represented by its coordinates in the Geographic coordi-
nate system specified as a pair of latitude and longitude values.
Geo-fence : A circular region set up around a beacon with its size given as the radius
starting from the center point i.e. the coordinates of the beacon.

#### 4.1.4 Functional Requirements

- The user should be able to create an account with a combination of an email address
    and a password.
- The users should be able to authenticate themselves with their created email and
    password accounts or via third-party providers such as Google or Facebook ac-
    counts.
- The user should be able to create exploration sessions after being localized by the
    application and specifying the desired details such as radius of search, the limit im-
    posed on the number of landmarks to be found, as well as the preferred categories.
    After searching for points of interest based on the above criteria, the application
    should present the number of landmarks discovered. The user then should have the
    option to start the session or adjust the search criteria. After starting a session, the
    relevant information should be saved on the cloud and be cached locally on the de-
    vice by the application such that it will not further rely on internet access for basic
    operations.
- The user may save their active session progress at any time to synchronize their
    locally cached data with the cloud database.
- The user may finalize a session prematurely, to which the application should react
    by pushing the recorded session and landmark information to the cloud database
    and wiping the local cache.


- After logging out of the application, any active session started by the user is sus-
    pended, allowing him/her to resume it after relogging.
- The application should provide means for resuming, finishing, and synchronizing
    their active session backed up on the cloud on any logged in device.
- When there is an active session, the application must provide a mechanism for guid-
    ing the user to the closest beacon set up by showing the general direction and dis-
    tance from it.
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
- The users should be able to view their finished sessions and their discovered land-
    marks along with their respective images on any logged in device.

#### 4.1.5 Non-functional Requirements


**Usability requirements**

- The application should provide clear indications related to its usage and under-
    standable error messages in the case of unexpected faults.


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
- During active session navigation, the application should receive frequent loca-
    tion updates in order to provide the most accurate directions possible.
- The navigation mechanisms relying on device sensor readings such as accelerom-
    eters or geomagnetic field sensors should work consistently from any point of
    the globe without being decalibrated by the geomagnetic fields.
- Geo-fence trigger events should be triggered in at most 20 seconds from enter-
    ing the specified radius.
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

#### 4.4.1 Technologies & Libraries

Regarding the technologies and libraries used during implementation the follow-
ing key ones may be evidentiated:

- **Firebase** : A number of services offered within the Firebase development plat-
    form have been used given the level of abstraction provided by them on top
    of the implementation details of the application’s cloud interaction needs. The
    respective services employed: **Authorization** : for managing user authentication
    and data management, **Firestore** : for persisting session data online within a well-
    optimized NoSQL database, **Storage** : for backing up landmark images captured
    by the users within their discovery sessions, and **MLKit** : for making use of the
    large volume of images and metadata available on the cloud for image labeling
    and landmark detection.
- **RxJava & RxAndroid** : Reactive Extensions provide the primary mechanism for
    facilitating the interaction between components of the system, having all opera-
    tions of the interface adapters communicate via reactive sources.
- **Lifecycle-Aware components and ViewModel** : For the implementation of the
    presentation layer in a lifecycle-conscious manner, making sure observable stream
    subscriptions and ViewModel presence are managed without being impacted by
    the changes in the state of Android’s UI components.
- **Retrofit** : Providing a concise interface for creating and managing REST API re-
    quests with the possibility of wrapping them into observable sources.
- **Room Persistence Library** : For the implementation of the local session cache,
    providing a level of abstraction on top of Android’s SQLite system database.
- **WorkManager** : For enqueueing and executing long-running background tasks
    which may impose certain constraints on their execution such as network access
    or storage availability.
- **Navigation Component** : For implementing the Single Activity Model as pre-
    sented in Section 3.3, essentially defining the flow of the application’s UI naviga-
    tion in a concise and clear manner, having it described as a collection of destina-
    tions and possible navigation actions between said destinations.
