package me.melontini.andromeda.base.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import me.melontini.andromeda.api.ApiRoute;
import me.melontini.andromeda.api.ModuleApi;

public final class ApiContainer {

  private final Map<ApiRoute<?, ?>, ModuleApi<?, ?>> apiMap = new HashMap<>();
  private final Map<ApiRoute<?, ?>, List<Consumer<ModuleApi<?, ?>>>> queue = new HashMap<>();

  public synchronized <I, O> void awaitRequest(
      ApiRoute<I, O> route, Consumer<ModuleApi<I, O>> consumer) {
    var api = apiMap.get(route);
    if (api != null) {
      consumer.accept((ModuleApi<I, O>) api);
      return;
    }
    this.queue.computeIfAbsent(route, m -> new ArrayList<>()).add((Consumer<ModuleApi<?, ?>>)
        (Object) consumer);
  }

  public synchronized <I, O> void propagateApi(ApiRoute<I, O> route, ModuleApi<I, O> api) {
    var queue = this.queue.get(route);
    if (queue != null) {
      for (Consumer<ModuleApi<?, ?>> consumer : queue) {
        consumer.accept(api);
      }
    }
    this.apiMap.put(route, api);
  }
}
