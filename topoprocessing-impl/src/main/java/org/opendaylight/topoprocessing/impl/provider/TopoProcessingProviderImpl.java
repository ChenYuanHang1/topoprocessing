/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.topoprocessing.impl.provider;

import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMDataChangeListener;
import org.opendaylight.controller.sal.core.api.model.SchemaService;
import org.opendaylight.topoprocessing.impl.listener.GlobalSchemaContextListener;
import org.opendaylight.topoprocessing.impl.listener.TopologyRequestListener;
import org.opendaylight.topoprocessing.impl.util.GlobalSchemaContextHolder;
import org.opendaylight.topoprocessing.spi.provider.TopoProcessingProvider;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;

/**
 * @author michal.polkorab
 *
 */
public class TopoProcessingProviderImpl implements TopoProcessingProvider {

    private DOMDataBroker dataBroker;
    private ListenerRegistration<DOMDataChangeListener> topologyRequestListenerRegistration;
    private SchemaService schemaService;
    private ListenerRegistration<SchemaContextListener> schemaContextListenerRegistration;

    /**
     * @param schemaService 
     * @param dataBroker
     */
    public TopoProcessingProviderImpl(SchemaService schemaService, DOMDataBroker dataBroker) {
        this.schemaService = schemaService;
        this.dataBroker = dataBroker;
        GlobalSchemaContextHolder.setSchemaContext(schemaService.getGlobalContext());
        startup();
    }

    @Override
    public void startup() {
        schemaContextListenerRegistration =
                schemaService.registerSchemaContextListener(new GlobalSchemaContextListener());
        registerTopologyRequestListener();
    }

    @Override
    public void close() throws Exception {
        schemaContextListenerRegistration.close();
        topologyRequestListenerRegistration.close();
    }

    private void registerTopologyRequestListener() {
        YangInstanceIdentifier identifier =
                YangInstanceIdentifier.of(NetworkTopology.QNAME).node(Topology.QNAME);

        topologyRequestListenerRegistration =
                dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                        identifier, new TopologyRequestListener(), DataChangeScope.BASE);
    }
}
