package com.aol.cyclops.functions.collections.extensions.persistent.lazy;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.functions.collections.extensions.AbstractLazyTest;

public class LazyPStackXTest extends AbstractLazyTest{

	@Override
	public <T> CollectionX<T> of(T... values) {
		PStackX<T> list = PStackX.empty();
		for(T next : values){
			list = list.plus(list.size(),next);
		}
		System.out.println("List " + list);
		return list.efficientOpsOff();
		
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> CollectionX<T> empty() {
		return PStackX.empty();
	}
}
