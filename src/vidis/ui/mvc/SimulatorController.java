/*	VIDIS is a simulation and visualisation framework for distributed systems.
	Copyright (C) 2009 Dominik Psenner, Christoph Caks
	This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
	This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
	You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>. */
package vidis.ui.mvc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import vidis.data.sim.AComponent;
import vidis.data.sim.SimNode;
import vidis.sim.Simulator;
import vidis.sim.classloader.modules.impl.AModuleFile;
import vidis.sim.classloader.modules.impl.dir.FileModuleFile;
import vidis.ui.config.Configuration;
import vidis.ui.events.IVidisEvent;
import vidis.ui.events.JobAppend;
import vidis.ui.events.VidisEvent;
import vidis.ui.events.jobs.jobs.layouts.ALayoutJob;
import vidis.ui.events.jobs.jobs.layouts.ARelayoutJob;
import vidis.ui.events.jobs.jobs.layouts.GraphElectricSpringLayoutJob;
import vidis.ui.model.graph.layouts.IGraphLayout;
import vidis.ui.model.graph.layouts.impl.GraphElectricSpringLayout;
import vidis.ui.model.graph.layouts.impl.GraphGridLayout;
import vidis.ui.model.graph.layouts.impl.GraphRandomLayout;
import vidis.ui.model.graph.layouts.impl.GraphSpiralLayout;
import vidis.ui.mvc.api.AController;
import vidis.ui.mvc.api.Dispatcher;
import vidis.util.ResourceManager;

public class SimulatorController extends AController {
	private static Logger logger = Logger.getLogger( SimulatorController.class );

	private Simulator sim = Simulator.getInstance();

	private IGraphLayout lastLayout = null;
	
	public SimulatorController() {
		logger.debug( "Constructor()" );
		
		registerEvent( IVidisEvent.InitSimulator );
		
		registerEvent( IVidisEvent.SimulatorPlay, 
						IVidisEvent.SimulatorLoad,
						IVidisEvent.SimulatorReload, 
						IVidisEvent.SimulatorPause );
		
		registerEvent(
				IVidisEvent.LayoutApplyGraphElectricSpring, 
				IVidisEvent.LayoutApplyRandom,
				IVidisEvent.LayoutApplySpiral,
				IVidisEvent.LayoutApplyGrid,
				IVidisEvent.LayoutReLayout
		);
		
		registerEvent(IVidisEvent.ExportSimFile);
	}
	
	@Override
	public void handleEvent(IVidisEvent event) {
		logger.debug( "handleEvent( "+event+" )" );
		switch ( event.getID() ) {
		case IVidisEvent.InitSimulator:
			initialize();
			break;
		case IVidisEvent.SimulatorPlay:
			sim.getPlayer().play();
			break;
		case IVidisEvent.SimulatorPause:
			sim.getPlayer().pause();
			break;
		case IVidisEvent.ExportSimFile:
			sim.exportSimFile(new File("export/out.msim"));
			break;
		case IVidisEvent.SimulatorLoad:
			if(event instanceof VidisEvent) {
				// now pick a file
				AModuleFile f = null;
				try {
					f = (AModuleFile) ((VidisEvent)event).getData();
				} catch(ClassCastException e) {
					logger.error("catched and resolved exception; older versions may use java.util.File, so let's try fallback.", e);
					try {
						f = new FileModuleFile((File) ((VidisEvent)event).getData());
					} catch(ClassCastException e2) {
						logger.error("catched BUT COULD NOT RECOVER: ",e2);
					}
				} finally {
					if(f != null) {
	//					System.err.println("Loading MSIM: " + f);
						// stop simulator
						if(!Simulator.getInstance().getPlayer().isPaused())
							Simulator.getInstance().getPlayer().pause();
						Simulator.getInstance().getPlayer().stop();
						
						// load file
						Simulator.getInstance().importSimFile(f);
						
						if(Configuration.USE_AUTOMATIC_DETAIL_LEVEL) {
							Dispatcher.forwardEvent(IVidisEvent.AutoAdjustDetailLevel);
						}
						
						// apply a nice layout
						Dispatcher.forwardEvent( IVidisEvent.LayoutApplySpiral );
					}
				}
				
//				if ( f != null && f.exists() && f.isFile()) {
//					if(!Simulator.getInstance().getPlayer().isPaused())
//						Simulator.getInstance().getPlayer().pause();
//					Simulator.getInstance().getPlayer().stop();
//					sim.importSimFile(f);
//					Configuration.DETAIL_LEVEL = 0.0;
////					Dispatcher.forwardEvent( IVidisEvent.LayoutApplyGraphElectricSpring );
//					Dispatcher.forwardEvent( IVidisEvent.LayoutApplyGrid );
//				}
			}
			break;
		case IVidisEvent.SimulatorReload:
			sim.reload();
			Dispatcher.forwardEvent( IVidisEvent.LayoutApplySpiral );
			break;
		case IVidisEvent.LayoutReLayout:
			Dispatcher.forwardEvent(
					new JobAppend( new ARelayoutJob() {
						public IGraphLayout getLayout() {
							return SimulatorController.this.lastLayout;
						}
						public Collection<SimNode> getNodes() {
							return SimulatorController.this.getNodes();
						}
						@Override
						public String toString() {
							return "Relayout Layout Job";
						}
					})
					);
			if(Configuration.USE_AUTOMATIC_DETAIL_LEVEL) {
				Dispatcher.forwardEvent(IVidisEvent.AutoAdjustDetailLevel);
			}
			break;
		case IVidisEvent.LayoutApplyGraphElectricSpring:
			lastLayout  = GraphElectricSpringLayout.getInstance();
			Dispatcher.forwardEvent( new JobAppend (new GraphElectricSpringLayoutJob() {
				public IGraphLayout getLayout() {
					return GraphElectricSpringLayout.getInstance();
				}
				public Collection<SimNode> getNodes() {
					return SimulatorController.this.getNodes();
				}
				@Override
				public String toString() {
					return "Electric Spring Layout Job";
				}
			}));
			if(Configuration.USE_AUTOMATIC_DETAIL_LEVEL) {
				Dispatcher.forwardEvent(IVidisEvent.AutoAdjustDetailLevel);
			}
			break;
		case IVidisEvent.LayoutApplyRandom:
			try {
				lastLayout  = GraphRandomLayout.getInstance();
				Dispatcher.forwardEvent( new JobAppend (new ALayoutJob() {
					public IGraphLayout getLayout() {
						return GraphRandomLayout.getInstance();
					}
					public Collection<SimNode> getNodes() {
						return SimulatorController.this.getNodes();
					}
					@Override
					public String toString() {
						return "Random Layout Job";
					}
				}));
				} catch (Exception e) {
					logger.error(e);
				}
				if(Configuration.USE_AUTOMATIC_DETAIL_LEVEL) {
					Dispatcher.forwardEvent(IVidisEvent.AutoAdjustDetailLevel);
				}
			break;
		case IVidisEvent.LayoutApplySpiral:
			try {
				lastLayout  = GraphSpiralLayout.getInstance();
				Dispatcher.forwardEvent( new JobAppend (new ALayoutJob() {
					public IGraphLayout getLayout() {
						return GraphSpiralLayout.getInstance();
					}
					public Collection<SimNode> getNodes() {
						return SimulatorController.this.getNodes();
					}
					@Override
					public String toString() {
						return "Spiral Layout Job";
					}
				}));
				} catch (Exception e) {
					logger.error(e);
				}
				if(Configuration.USE_AUTOMATIC_DETAIL_LEVEL) {
					Dispatcher.forwardEvent(IVidisEvent.AutoAdjustDetailLevel);
				}
			break;
		case IVidisEvent.LayoutApplyGrid:
			try {
				logger.info("sending grid layout job");
				lastLayout  = GraphGridLayout.getInstance();
				Dispatcher.forwardEvent( new JobAppend (new ALayoutJob() {
					public IGraphLayout getLayout() {
						return GraphGridLayout.getInstance();
					}
					public Collection<SimNode> getNodes() {
						return SimulatorController.this.getNodes();
					}
					@Override
					public String toString() {
						return "Grid Layout Job";
					}
				}));
				} catch (Exception e) {
					logger.error(e);
				}
				if(Configuration.USE_AUTOMATIC_DETAIL_LEVEL) {
					Dispatcher.forwardEvent(IVidisEvent.AutoAdjustDetailLevel);
				}
			break;
		}
	}
	
	private void initialize() {
//		Simulator.createInstance();
//		sim = Simulator.getInstance();
//		sim.importSimFile( ResourceManager.getModuleFile("bullyElectionAlgorithm", "demo.msim") );
//		sim.importSimFile( ResourceManager.getModuleFile("demo", "demo.msim") );
//		sim.importSimFile( ResourceManager.getModuleFile("demo", "simpledemo.msim") );
//		sim.importSimFile( ResourceManager.getModuleFile("flooding", "flood1.msim") );
//		sim.importSimFile( ResourceManager.getModuleFile("flooding", "v1.msim") );
//		sim.importSimFile( ResourceManager.getModuleFile("vartest", "onenode.msim") );
//		sim.importSimFile( ResourceManager.getModuleFile("vectorClockAlgorithm", "simple.msim") );
//		sim.importSimFile( ResourceManager.getModuleFile("vectorClockAlgorithm", "complex.msim") );
//		sim.importSimFile( ResourceManager.getModuleFile("vectorClockAlgorithm", "veryComplex.msim") );
//		sim.importSimFile( ResourceManager.getModuleFile("vectorClockAlgorithm", "veryComplexLoose.msim") );
		// apply default layout
//		layout();
		// start playing
		//sim.getPlayer().play();
//		Dispatcher.forwardEvent( IVidisEvent.LayoutApplyGraphElectricSpring );
//		Dispatcher.forwardEvent( new VidisEvent<File>( IVidisEvent.SimulatorLoad, ResourceManager.getModuleFile("vectorClockAlgorithm", "simple.msim") ) );
	}
	
	private List<SimNode> getNodes() {
		List<AComponent> components = sim.getSimulatorComponents();
		List<SimNode> nodes = new ArrayList<SimNode>();
		for(AComponent component : components) {
			if(component instanceof SimNode) {
				nodes.add((SimNode) component);
			}
		}
		return nodes;
	}
}
