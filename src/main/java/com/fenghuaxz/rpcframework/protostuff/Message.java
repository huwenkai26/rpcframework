//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtainType a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package com.fenghuaxz.rpcframework.protostuff;

/**
 * The serializable object where its {@link Schema schema} handles its serialization and deserialization.
 *
 * @author David Yu
 * @created Nov 9, 2009
 */
public interface Message<T> {

    /**
     * Gets the cached schema of this pojo.
     */
    Schema<T> cachedSchema();

}
