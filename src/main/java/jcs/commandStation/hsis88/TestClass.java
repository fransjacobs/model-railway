/*
 * Copyright 2024 fransjacobs.
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
 */
package jcs.commandStation.hsis88;

import jcs.util.ByteUtil;

/**
 *
 * @author fransjacobs
 */
public class TestClass {
  
  
  //private static void setContactValues(int hl,byte)
  
  
  public static void main(String[] a) {
    
  
//          int moduleNr = module.getModuleNumber();
      int low = 3; //module.getLowByte();
      int high = 0; //module.getHighByte();
      int[] contacts = new int[16];
      int hlidx=0;
      for(int i=0;i<8;i++) {
        int m = ((int)Math.pow(2,i));
        int pv = (low & m) > 0 ? 1 : 0;
        
    System.out.println("i: "+i+" m: 0x"+ByteUtil.toHexString(m)+" cv["+(i+hlidx)+"]: "+pv);
        
         
        
      }
 
      
//           int c1 = (low & 0x01) > 0 ? 1 : 0;
//      int c2 = (low & 0x02) > 0 ? 2 : 0;
//      int c3 = (low & 0x04) > 0 ? 3 : 0;
//      int c4 = (low & 0x08) > 0 ? 4 : 0;
//      int c5 = (low & 0x10) > 0 ? 5 : 0;
//      int c6 = (low & 0x20) > 0 ? 6 : 0;
//      int c7 = (low & 0x40) > 0 ? 7 : 0;
//      int c8 = (low & 0x80) > 0 ? 8 : 0;
//     
//      int c9 = (high & 0x01) > 0 ? 9 : 0;
//      int c10 = (high & 0x02) > 0 ? 10 : 0;
//      int c11 = (high & 0x04) > 0 ? 11 : 0;
//      int c12 = (high & 0x08) > 0 ? 14 : 0;
//      int c13 = (high & 0x10) > 0 ? 13 : 0;
//      int c14 = (high & 0x20) > 0 ? 14 : 0;
//      int c15 = (high & 0x40) > 0 ? 15 : 0;
//      int c16 = (high & 0x80) > 0 ? 16 : 0;
//
//      
      
      
// In c ;
//Int number=1, declare i also;
//For(i=0;I<=10;I++){
////   1.        =.    1.      *2 then number =2 
////.   2.       =.    2.     *2 then number =4
////.   4.       =.    4.     *2 then number =8
////.   2.       =.    8.     *2 then number =16
//-----
//   number=number*2;
//   Print("%d",number);
//}
//Output
//2481632-----
      
      

//      int c1 = (low & 0x01) > 0 ? 1 : 0;
//      int c2 = (low & 0x02) > 0 ? 2 : 0;

    
    
    
  }
  
  
  
  
  
  
}
