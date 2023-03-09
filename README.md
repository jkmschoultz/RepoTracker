
# RepoTracker

## Introduction
This is the RepoTracker app. It is an app that provides users with basic information for GitHub repositories by using the GitHub API. It allows users to keep track of their own repositories as well as search for any other public repositories to track any updates. The app has been built for a target API of 31.

On launching the app, users will be asked to provide their GitHub username in order to allow the app to find repositories owned by the user. This username will be saved to the app as a shared preference, allowing the user to close and reopen the app without needing to input their username repeatedly. After completing this, the app will redirect users to the main activity where they can search for any public GitHub repositories. The results of a search, if any, will be displayed in a RecyclerView that provides key information for the repositories matching the search. This includes the name, owner, and number of forks and issues for the repository. Users can click a repository from this list in order to be redirected to the homepage of the repository on GitHub, through the use of an implicit intent.

There is an options menu in the top right that allows users to navigate between this main activity and others through explicit intents, as well as other customization settings. The “My Repositories” option directs users to a new activity that will display the GitHub repositories owned by the username provided when the app was initially launched. This uses the same RecyclerView layout as the main activity, also providing a way of navigating to a user’s repository page on GitHub. The “Set User” option allows users to change the GitHub username stored by the app. The “Toggle Dark Mode” setting allows a user to change the display of the app between light modes and dark modes, depending on their preference. The “Reset” button clears any saved data, redirecting users back to the original “Set User” activity.

## Design Rationale
In designing this app, I chose to use activities to separate the various user interfaces. Fragments could equally be used to navigate the different interactions, although I determined that using activities would allow for the options menu to appear differently in the various contexts. For example, the search bar in the main activity should not be visible when the user is viewing the list of their own repositories, and in the “Set User” activity the options menu will not be present at all.

When creating the two activities that contain the RecyclerView, I decided to create a BaseActivity that is extended by both activities, which consists of all the common features of both activities. This allowed for a more elegant design that allows both activities to inherit common features from the BaseActivity, while the difference in features can be more clearly evident in each of the individual activities. This includes the reliance of the user’s repositories activity on the saved username shared preference, as well as the different paths used when interacting with the GitHub API.

I also decided to use shared preferences to store the user’s saved data, as the data being stored could sufficiently be represented by primitive data types without the need for a larger database storage. The username can be effectively saved as a string stored in a shared preference key-value pair.

## Challenges
The main challenge faced in creating this app was determining how much information to store between saved instance states. At first, I attempted to save the entire list of elements in the RecyclerView, which would allow a user to retain any search made between instance states. However, this resulted in too much information being passed between states for larger lists of repositories. Instead, I decided it would suffice to save the search query entered by the user (or access the username from the shared preference, depending on the activity) which would allow the app to reobtain the list of results searched, as well as refresh the results to provide the latest up-to-date changes in data that may be made.

Another challenge was enabling users to reach a given GitHub repository by clicking on a specific ViewHolder instance. I was able to overcome this by storing the url path to a repository as a local variable in the ViewHolder class. This variable is then updated when the ViewHolder is binded to new data, allowing the ViewHolder to launch the implicit intent for the relevant repository link when it is pressed by the user.

### Potential Improvements
To further develop this app, the level at which it interacts with the GitHub API could be expanded upon. Users could instead be asked to log in to their GitHub account, which would allow the app to display all the user’s private repositories in addition to the public repositories currently being displayed.

Having the app be linked to multiple GitHub accounts could also be another development. This would allow a user to track repositories owned by other GitHub accounts that are of interest to the user, in addition to keeping track of their own.
