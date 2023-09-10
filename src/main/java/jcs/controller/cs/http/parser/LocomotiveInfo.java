/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.controller.cs.http.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jcs.controller.cs.can.CanMessage;

/**
 *
 * @author Frans Jacobs
 */
public class LocomotiveInfo implements Serializable {

  public LocomotiveInfo(CanMessage statusRequest) {
    parseMessage(statusRequest);
  }

  public LocomotiveInfo(String serialNumber, String catalogNumber, String description) {
//    this.serialNumber = serialNumber;
//    this.catalogNumber = catalogNumber;
//    this.description = description;
  }

//  void ProtocolMaerklinCAN::ParseCommandConfigData(const unsigned char* const buffer)
//	{
//		CanLength length = ParseLength(buffer);
//		switch (length)
//		{
//			case 6:
//			case 7:
//				ParseCommandConfigDataFirst(buffer);
//				return;
//
//			case 8:
//				ParseCommandConfigDataNext(buffer);
//				return;
//
//			default:
//				return;
//		}
//		return;
//	}
//	void ProtocolMaerklinCAN::ParseCommandConfigDataFirst(const unsigned char* const buffer)
//	{
//		if (canFileData != nullptr)
//		{
//			free(canFileData);
//		}
//		canFileDataSize = Utils::Utils::DataBigEndianToInt(buffer + 5);
//		canFileCrc = Utils::Utils::DataBigEndianToShort(buffer + 9);
//		canFileData = reinterpret_cast<unsigned char*>(malloc(canFileDataSize + 8));
//		canFileDataPointer = canFileData;
//	}
//
//	void ProtocolMaerklinCAN::ParseCommandConfigDataNext(const unsigned char* const buffer)
//	{
//		if (canFileData == nullptr)
//		{
//			return;
//		}
//
//		Utils::Utils::Copy8Bytes(buffer + 5, canFileDataPointer);
//		canFileDataPointer += 8;
//		if (canFileDataSize > static_cast<size_t>(canFileDataPointer - canFileData))
//		{
//			return;
//		}
//
//		size_t canFileUncompressedSize = Utils::Utils::DataBigEndianToInt(canFileData);
//		logger->Info(Languages::TextConfigFileReceivedWithSize, canFileUncompressedSize);
//		string file = ZLib::UnCompress(reinterpret_cast<char*>(canFileData + 4), canFileDataSize, canFileUncompressedSize);
//		deque<string> lines;
//		Utils::Utils::SplitString(file, "\n", lines);
//		for(std::string& line : lines)
//		{
//			logger->Debug(line);
//		}
//		ParseCs2File(lines);
//
//		free(canFileData);
//	 	canFileDataSize = 0;
//	 	canFileData = nullptr;
//	 	canFileDataPointer = nullptr;
//	}
  private void parseMessage(CanMessage statusRequest) {
    //First byte holde the serial
    byte[] data1 = statusRequest.getResponse(0).getData();
    byte[] sn = new byte[2];
    System.arraycopy(data1, 6, sn, 0, sn.length);

    int serial = ((sn[0] & 0xFF) << 8) | (sn[1] & 0xFF);
//    this.serialNumber = serial + "";

    if (statusRequest.getResponses().size() > 1) {
      //Second holds the catalog numer is asci
      byte[] data2 = statusRequest.getResponse(1).getData();
      //catalogNumber = Base64.getEncoder().encodeToString(data2);
//      catalogNumber = dataToString(data2);
    }

    if (statusRequest.getResponses().size() > 2) {
      //Third is description
      byte[] data3 = statusRequest.getResponse(2).getData();
      //description = Base64.getEncoder().encodeToString(data3);
//      description = dataToString(data3);
    }

    if (statusRequest.getResponses().size() > 3) {
      //Fourth is description
      byte[] data4 = statusRequest.getResponse(3).getData();
      //description = description + Base64.getEncoder().encodeToString(data4);
//      description = description + dataToString(data4);
    }
    //uid is in the request
//    uid = statusRequest.getUidInt();
  }

  private static String dataToString(byte[] data) {
    //filter out 0 bytes
    List<Byte> bl = new ArrayList<>();
    for (int i = 0; i < data.length; i++) {
      if (data[i] > 0) {
        bl.add(data[i]);
      }
    }
    byte[] bytes = new byte[bl.size()];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = bl.get(i);
    }
    return new String(bytes);
  }

}
