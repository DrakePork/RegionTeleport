package src.com.drakepork.regionteleport.Utils;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import src.com.drakepork.regionteleport.RegionTeleport;

public class PluginReceiver extends AbstractModule {

	protected final RegionTeleport plugin;

	public PluginReceiver(RegionTeleport plugin) {
		this.plugin = plugin;
	}

	public Injector createInjector() {
		return Guice.createInjector(this);
	}

	@Override
	protected void configure() {
		this.bind(RegionTeleport.class).toInstance(this.plugin);
	}
}
