package me.fourteendoggo.xkingdoms.storage;

import me.fourteendoggo.xkingdoms.home.Home;

import java.util.Set;
import java.util.UUID;

public interface Storage<T> {

    T loadHomes(UUID owner);

    T createHome(Home home);

    T saveHomes(Set<Home> homes);
}
