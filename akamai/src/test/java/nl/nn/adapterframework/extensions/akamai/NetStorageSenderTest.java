/*
   Copyright 2020 WeAreFrank!

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
package nl.nn.adapterframework.extensions.akamai;

import static nl.nn.adapterframework.testutil.TestAssertions.assertEqualsIgnoreCRLF;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import nl.nn.adapterframework.core.PipeLineSession;
import nl.nn.adapterframework.core.SenderException;
import nl.nn.adapterframework.extensions.akamai.NetStorageSender.Action;
import nl.nn.adapterframework.http.HttpResponseHandler;
import nl.nn.adapterframework.http.HttpSenderTestBase;
import nl.nn.adapterframework.parameters.Parameter;
import nl.nn.adapterframework.stream.Message;
import nl.nn.adapterframework.util.AppConstants;

public class NetStorageSenderTest extends HttpSenderTestBase<NetStorageSender> {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		AppConstants.getInstance().setProperty("http.headers.messageid", false);
	}

	@Override
	public NetStorageSender createSender() {
		return spy(new NetStorageSender() {
			@Override
			public Message extractResult(HttpResponseHandler responseHandler, PipeLineSession session) throws SenderException, IOException {
				return new Message( getResponseBodyAsString(responseHandler, true) );
			}
		});
	}

	@Override
	public NetStorageSender getSender() throws Exception {
		NetStorageSender sender = super.getSender(false);
		sender.setCpCode("cpCode123");
		sender.setNonce("myNonce");
		sender.setAccessToken(null); //As long as this is NULL, X-Akamai-ACS-Auth-Sign will be NULL
		return sender;
	}

	@Test
	public void testContentType() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.DU);
		sender.configure();
		assertNull("no content-type should be present", sender.getFullContentType());
	}

	@Test
	public void duAction() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.DU);
		Message input = new Message("my/special/path/"); //Last slash should be removed!

		try {
			PipeLineSession pls = new PipeLineSession(session);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("duAction.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void duActionWithRootDir() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.DU);
		sender.setRootDir("/my/special/"); //Start and end with a slash!
		Message input = new Message("path/"); //Last slash should be removed!

		try {
			PipeLineSession pls = new PipeLineSession(session);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("duAction.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void dirAction() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.DIR);
		Message input = new Message("my/special/path/");

		try {
			PipeLineSession pls = new PipeLineSession(session);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("dirAction.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void deleteAction() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.DELETE);
		Message input = new Message("my/special/path/");

		try {
			PipeLineSession pls = new PipeLineSession(session);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("deleteAction.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void uploadAction() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.UPLOAD);
		Message input = new Message("my/special/path/");

		Parameter param = new Parameter();
		param.setName("file");
		param.setSessionKey("fileMessage");
		sender.addParameter(param);
		try {
			Message file = new Message("<dummyFile>");
			PipeLineSession pls = new PipeLineSession(session);
			pls.put("fileMessage", file);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("uploadAction.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void uploadActionWithMD5Hash() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.UPLOAD);
		sender.setHashAlgorithm(HashAlgorithm.MD5);
		Message input = new Message("my/special/path/");

		Parameter param = new Parameter();
		param.setName("file");
		param.setSessionKey("fileMessage");
		sender.addParameter(param);
		try {
			Message file = new Message("<dummyFile>");
			PipeLineSession pls = new PipeLineSession(session);
			pls.put("fileMessage", file);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("uploadActionMD5.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void uploadActionWithCustomMD5Hash() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.UPLOAD);
		sender.setHashAlgorithm(HashAlgorithm.MD5);
		Message input = new Message("my/special/path/");

		Parameter hashParam = new Parameter();
		hashParam.setName("md5");
		hashParam.setValue("a1658c154b6af0fba9d93aa86e5be06f");//Matches response file but uses a different input message
		sender.addParameter(hashParam);

		Parameter param = new Parameter();
		param.setName("file");
		param.setSessionKey("fileMessage");
		sender.addParameter(param);
		try {
			Message file = new Message("<dummyFile>----");
			PipeLineSession pls = new PipeLineSession(session);
			pls.put("fileMessage", file);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("uploadActionMD5.txt"), result.replace("----", "").trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void uploadActionWithSHA1Hash() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.UPLOAD);
		sender.setHashAlgorithm(HashAlgorithm.SHA1);
		Message input = new Message("my/special/path/");

		Parameter param = new Parameter();
		param.setName("file");
		param.setSessionKey("fileMessage");
		sender.addParameter(param);
		try {
			Message file = new Message("<dummyFile>");
			PipeLineSession pls = new PipeLineSession(session);
			pls.put("fileMessage", file);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("uploadActionSHA1.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void uploadActionWithCustomSHA1Hash() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.UPLOAD);
		sender.setHashAlgorithm(HashAlgorithm.SHA1);
		Message input = new Message("my/special/path/");

		Parameter hashParam = new Parameter();
		hashParam.setName("sha1");
		hashParam.setValue("51e8bbf813bdbcede109d13b863a58132e80b2e2");//Matches response file but uses a different input message
		sender.addParameter(hashParam);

		Parameter param = new Parameter();
		param.setName("file");
		param.setSessionKey("fileMessage");
		sender.addParameter(param);
		try {
			Message file = new Message("<dummyFile>----");
			PipeLineSession pls = new PipeLineSession(session);
			pls.put("fileMessage", file);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("uploadActionSHA1.txt"), result.replace("----", "").trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void uploadActionWithSHA256Hash() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.UPLOAD);
		sender.setHashAlgorithm(HashAlgorithm.SHA256);
		Message input = new Message("my/special/path/");

		Parameter param = new Parameter();
		param.setName("file");
		param.setSessionKey("fileMessage");
		sender.addParameter(param);
		try {
			Message file = new Message("<dummyFile>");
			PipeLineSession pls = new PipeLineSession(session);
			pls.put("fileMessage", file);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("uploadActionSHA256.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void uploadActionWithCustomSHA256Hash() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.UPLOAD);
		Message input = new Message("my/special/path/");

		Parameter hashParam = new Parameter();
		hashParam.setName("sha256");
		hashParam.setValue("71d1503b5afba60e212a46e4112fba56503e281224957ad8dee6034ad25f12dc"); //Matches response file but uses a different input message
		sender.addParameter(hashParam);

		Parameter param = new Parameter();
		param.setName("file");
		param.setSessionKey("fileMessage");
		sender.addParameter(param);
		try {
			Message file = new Message("<dummyFile>----");
			PipeLineSession pls = new PipeLineSession(session);
			pls.put("fileMessage", file);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("uploadActionSHA256.txt"), result.replace("----", "").trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void mkdirAction() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.MKDIR);
		Message input = new Message("my/special/path/");

		try {
			PipeLineSession pls = new PipeLineSession(session);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("mkdirAction.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void rmdirAction() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.RMDIR);
		Message input = new Message("my/special/path/");

		try {
			PipeLineSession pls = new PipeLineSession(session);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("rmdirAction.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void renameAction() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.RENAME);
		Message input = new Message("my/special/path/file1.txt");

		Parameter param = new Parameter();
		param.setName("destination");
		param.setValue("my/other/special/path/file2.txt");
		sender.addParameter(param);
		try {
			PipeLineSession pls = new PipeLineSession(session);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("renameAction.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void mtimeAction() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.MTIME);
		Message input = new Message("my/special/path/");

		Parameter param = new Parameter();
		param.setName("mtime");
		param.setValue("1633945058");
		sender.addParameter(param);
		try {
			PipeLineSession pls = new PipeLineSession(session);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("mtimeAction.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void downloadAction() throws Throwable {
		NetStorageSender sender = getSender();
		sender.setAction(Action.DOWNLOAD);
		Message input = new Message("my/special/path/");

		try {
			PipeLineSession pls = new PipeLineSession(session);

			sender.configure();
			sender.open();

			String result = sender.sendMessage(input, pls).asString();
			assertEqualsIgnoreCRLF(getFile("downloadAction.txt"), result.trim());
		} catch (SenderException e) {
			throw e.getCause();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}
}