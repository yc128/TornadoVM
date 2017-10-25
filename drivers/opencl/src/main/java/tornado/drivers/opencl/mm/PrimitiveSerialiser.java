/*
 * This file is part of Tornado: A heterogeneous programming framework: 
 * https://github.com/beehive-lab/tornado
 *
 * Copyright (c) 2013-2017 APT Group, School of Computer Science, 
 * The University of Manchester
 *
 * This work is partially supported by EPSRC grants:
 * Anyscale EP/L000725/1 and PAMELA EP/K008730/1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: James Clarkson
 *
 */
package tornado.drivers.opencl.mm;

import java.nio.ByteBuffer;
import tornado.common.Tornado;

public class PrimitiveSerialiser {

	
	private static final void align(ByteBuffer buffer, int align){
		while(buffer.position() % align != 0)
			buffer.put((byte) 0);
	}
	
	public static final void put(ByteBuffer buffer, Object value){
		put(buffer,value,0);
	}
	
	public static final void put(ByteBuffer buffer, Object value, int alignment){
		if(value instanceof Integer){
			buffer.putInt((int) value);
		} else if(value instanceof Long){
			buffer.putLong((long) value);
		} else if (value instanceof Short){
			buffer.putShort((short) value);
		} else if (value instanceof Float){
			buffer.putFloat((float) value);
		} else if (value instanceof Double){
			buffer.putDouble((double) value);
		} else {
			Tornado.warn("unable to serialise: %s (%s)",value,value.getClass().getName());
		}
		
		if(alignment != 0)
			align(buffer,alignment);
	}
	
}
