package com.app.srivyaradio.utils

object Constants {
    const val LOCATION_API_URL = "http://ip-api.com/"
    const val STATIONS_API_URL = "https://srivyaa.github.io/RadioStations/"
    const val FOLDERS_API_URL = "https://srivyaa.github.io/RadioStations/data/folders/"
    const val COUNTRY_CODE = "countryCode"
    const val IS_FIRST_START = "isFirstStartup"
    const val MODE = "mode"
    const val LAST_PLAY = "lastPlay"
    const val VERSION = "version"
    const val HAS_PURCHASED = "hasPurchased"
    const val DATABASE = "database"
    const val SHARED_PREF = "radiotimepref"
    const val FAVORITES_ID = "favorites"
    const val DISCOVER_ID = "discover"
    const val ROOT_ID = "root"
    const val COUNTRIES_ID = "countries"
    const val BROWSE_ID = "browse"
    const val BROWSE_FOLDER_PREFIX = "browse_folder:"
    const val OFFLINE_ID = "offline"
    const val MORE_ID = "more"
    const val ALPHABET_PREFIX = "alpha:"
    const val COUNTRY_PREFIX = "country:"
    const val CHANGE_COUNTRY_KEY = "KEY_CHANGE_COUNTRY"
    const val CHANGE_COUNTRY_COMMAND = "COMMAND_CHANGE_COUNTRY"
    const val UPDATE_FAVORITE_COMMAND = "COMMAND_UPDATE_FAVORITE"
    const val SET_TIMER_KEY = "KEY_SET_TIMER"
    const val SET_TIMER_COMMAND = "COMMAND_SET_TIMER"
    const val TOGGLE_FAVORITE_COMMAND = "COMMAND_TOGGLE_FAVORITE"
    const val TOGGLE_FAVORITE_STATION_ID_KEY = "KEY_TOGGLE_FAVORITE_STATION_ID"
    const val RADIO_LOGO = "https://srivyaa.github.io/ImageHost/ic_radiologo.png"
    // New preferences
    const val DEFAULT_SCREEN = "defaultScreen" // values: Favorites, Discover, Recents
    const val USER_COUNTRIES = "userCountries" // serialized as name|code;name|code;...
    const val RECENTS_LIST = "recentsList" // comma-separated station IDs
    // Playback commands
    const val TOGGLE_SHUFFLE_COMMAND = "COMMAND_TOGGLE_SHUFFLE"
    const val CYCLE_REPEAT_COMMAND = "COMMAND_CYCLE_REPEAT"
    const val SEEK_BACK_COMMAND = "COMMAND_SEEK_BACK"
    const val SEEK_FORWARD_COMMAND = "COMMAND_SEEK_FORWARD"

    // Wear OS Data Layer paths and actions
    const val WEAR_CONTROL_PATH = "/srivyaradio/control"
    const val WEAR_ACTION_PLAY_PAUSE = "PLAY_PAUSE"
    const val WEAR_ACTION_NEXT = "NEXT"
    const val WEAR_ACTION_PREV = "PREV"
    const val WEAR_ACTION_SEEK_BACK = "SEEK_BACK"
    const val WEAR_ACTION_SEEK_FORWARD = "SEEK_FORWARD"
    const val WEAR_ACTION_TOGGLE_SHUFFLE = "TOGGLE_SHUFFLE"
    const val WEAR_ACTION_CYCLE_REPEAT = "CYCLE_REPEAT"
}