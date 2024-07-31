package me.melontini.andromeda.api;

/**
 * A declaration of some API route. A route has 3 different statuses representing its lifecycle.
 *
 * <ul>
 *     <li>
 *         {@link Status#EXPERIMENTAL} - The route is alive, but its stability is uncertain.
 *     </li>
 *     <li>
 *         {@link Status#STABLE} - The route is alive and stable.
 *     </li>
 *     <li>
 *         {@link Status#DEPRECATED} - The route is alive, but is deprecated and might be removed in a future update.
 *     </li>
 *     <li>
 *         {@link Status#DEAD} - The route is dead and will be removed in the next major Minecraft version.
 *     </li>
 * </ul>
 *
 * An appropriate message will be logged when a route is not {@link Status#STABLE}.
 */
public record ApiRoute<I, O>(String route, Status status) {

  public enum Status {
    EXPERIMENTAL,
    STABLE,
    DEPRECATED,
    DEAD
  }
}
