/*
   Copyright 2024 WeAreFrank!

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package nl.nn.adapterframework.jdbc.datasource;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * Extension of {@link PoolingDataSource} that exposes an extra method to fetch pool statistics.
 *
 * @param <C>
 */
public class OpenPoolingDataSource<C> extends PoolingDataSource {
	public OpenPoolingDataSource(final ObjectPool<C> pool) {
		super(pool);
	}

	public void addPoolMetadata(StringBuilder info) {
		ObjectPool<C> objectPool = super.getPool();
		if (objectPool instanceof GenericObjectPool) {
			GenericObjectPoolUtil.addPoolMetadata((GenericObjectPool<C>) objectPool, info);
		}
	}
}