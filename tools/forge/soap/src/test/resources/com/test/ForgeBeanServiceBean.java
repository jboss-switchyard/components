package com.test;

import javax.inject.Inject;

import org.switchyard.component.bean.Reference;
import org.switchyard.component.bean.Service;


public @Service(ForgeBeanService.class) class ForgeBeanServiceBean implements com.test.ForgeBeanService {

	@Inject @Reference
	public ForgeBeanServiceReferenceable referenceableService;

	@Override
    public void process(String content) {
        // Add processing logic here
    }
}
