package ch.makery.address.view;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.controlsfx.dialog.Dialogs;
import org.xml.sax.SAXException;

import ch.makery.address.util.UploadToFlickrUsingFlickr4Java;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.flickr4java.flickr.uploader.Uploader;
import com.sun.javafx.webkit.Accessor;
import com.sun.webkit.WebPage;

public class HtmlEditorController {

	// Flickr
	private String MY_FLICKR_CLIENT_API_KEY = "61f7a258f1918f307ca48ff6258ee7ec";
	private String MY_FLICKR_CLIENT_SHARED_SECRET = "1181a6039715511d";
	private String MY_FLICKR_CLIENT_NSID = "131030724@N02";
	// private String FLICKR_UPLOAD_URL = "https://api.imgur.com/3/image";
	private String MY_FLICKR_CLIENT_ACCESS_TOKEN = null; // Optional entry.
	private String MY_FLICKR_CLIENT_TOKEN_SECRET = null; // Optional entry.
	private String MY_FLICKR_CLIENT_USERNAME = null;
	private boolean MY_FLICKR_CLIENT_SET_TAG_NAME = true; // Default to true to
															// add tag while
															// uploading.
	// Flickr4Java
	UploadMetaData metaData;
	Flickr flickr;
	Uploader uploader;
	UploadToFlickrUsingFlickr4Java uploadToFlickrUsingFlickr4Java;
	ArrayList<String> optionArgs = new ArrayList<String>();
	private String authsDirStr = System.getProperty("user.home")
			+ File.separatorChar + ".flickrAuth";

	@FXML
	private HTMLEditor htmlEditor;
	@FXML
	private TextArea htmlDisplayArea;
	@FXML
	private WebView webView;
	private WebEngine webEngine;
	@FXML
	private ProgressIndicator indicator;
	@FXML
	private Label failedLabel;

	@FXML
	private void initialize() {
		metaData = new UploadMetaData();

		try {
			uploadToFlickrUsingFlickr4Java = new UploadToFlickrUsingFlickr4Java(
					MY_FLICKR_CLIENT_API_KEY, MY_FLICKR_CLIENT_NSID,
					MY_FLICKR_CLIENT_SHARED_SECRET, new File(authsDirStr),
					MY_FLICKR_CLIENT_USERNAME);
		} catch (FlickrException e) {
			e.printStackTrace();
		}

		uploadToFlickrUsingFlickr4Java
				.setSetorigfilenametag(MY_FLICKR_CLIENT_SET_TAG_NAME);
		try {
			uploadToFlickrUsingFlickr4Java.setAuth(
					MY_FLICKR_CLIENT_ACCESS_TOKEN, MY_FLICKR_CLIENT_USERNAME,
					MY_FLICKR_CLIENT_TOKEN_SECRET);
		} catch (IOException | SAXException | FlickrException e) {
			e.printStackTrace();
		}

		webEngine = webView.getEngine();
	}

	@FXML
	private void handleShowView() {
		showOnHTMLDisplayArea();
		showOnWebView();
	}

	@FXML
	private void showOnHTMLDisplayArea() {
		if (htmlEditor.getHtmlText() == null) {
			System.out.println("htmlEditor is null");
		} else if (htmlEditor.getHtmlText().isEmpty()) {
			System.out.println("htmlEditor is empty");
		} else {
			htmlDisplayArea.setText(htmlEditor.getHtmlText());
		}
	}

	@FXML
	private void showOnWebView() {
		if (htmlEditor.getHtmlText() == null) {
			System.out.println("htmlEditor is null");
		} else if (htmlEditor.getHtmlText().isEmpty()) {
			System.out.println("htmlEditor is empty");
		} else {
			webEngineLoadContent(htmlEditor.getHtmlText(), false);
		}
	}

	@FXML
	private void testGoogleOnWebView() {
		webEngineLoadContent("http://www.google.com", true);
	}

	private void webEngineLoadContent(String content, boolean url) {
		if (url) {
			webEngine.load(content);
		} else {
			webEngine.loadContent(content);
		}
		indicator.setVisible(true);
		indicator.progressProperty().bind(
				webEngine.getLoadWorker().progressProperty());
		webEngine.getLoadWorker().stateProperty()
				.addListener((ov, oldState, newState) -> {
					switch (newState) {
					case SUCCEEDED:
						webView.setVisible(true);
						indicator.setVisible(false);
						break;
					case FAILED:
						failedLabel.setVisible(true);
						break;
					default:
						break;
					}
					;
				});
	}

	@FXML
	private void handleChooseTemplate() {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		ExtensionFilter extFilter1 = new ExtensionFilter("HTML files (*.html)",
				"*.html");
		fileChooser.getExtensionFilters().add(extFilter1);

		fileChooser.setTitle("Choose Template");
		File file = fileChooser.showOpenDialog(null);

		if (file != null) {
			loadHTMLFile(file);
		}
	}

	/*
	 * Load HTML from file
	 */
	private void loadHTMLFile(File file) {
		try {
			StringBuilder sb = new StringBuilder();
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			while (bis.available() > 0) {
				sb.append((char) bis.read());
			}
			bis.close();

			String htmlContent = sb.toString();

			webEngineLoadContent(htmlContent, false);
			htmlEditor.setHtmlText(htmlContent);

		} catch (Exception e) { // catches ANY exception
			Dialogs.create()
					.title("Error")
					.masthead(
							"Could not load data from file:\n" + file.getPath())
					.showException(e);
		}
	}

	@FXML
	private void showOnHtmlEditor() {
		if (htmlDisplayArea.getText() == null) {
			System.out.println("htmlDisplayArea is null");
		} else if (htmlDisplayArea.getText().isEmpty()) {
			System.out.println("htmlDisplayArea is empty");
		} else {
			htmlEditor.setHtmlText(htmlDisplayArea.getText());
			showOnWebView();
		}
	}

	private void addImageToCaretPosition(String imgSrc) {
		try {
			// System.out.println("image: " + imgSrc);
			String imageSource = "<p>" + "<img alt= \"Sexy Lady!\" src=\""
					+ imgSrc + "\" width=\"500\" height=\"500\" />" + "</p>";
			WebView webView = (WebView) htmlEditor.lookup("WebView");
			WebPage webPage = Accessor.getPageFor(webView.getEngine());
			webPage.executeCommand("insertText", "PlaceImageHerePlease");
			String fullHtml = htmlEditor.getHtmlText();
			String correctHtml = fullHtml.replaceAll("PlaceImageHerePlease",
					imageSource);
			htmlEditor.setHtmlText(correctHtml);
			showOnWebView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleUploadImage() {
		FileChooser fileChooser = new FileChooser();

		List<String> extFilterImages = Arrays
				.asList("*.jpeg", "*.jpg", "*.png");

		// Set extension filter
		ExtensionFilter extFilterAllImage = new ExtensionFilter(
				"JPEG, JPG & PNG", extFilterImages);
		fileChooser.getExtensionFilters().addAll(extFilterAllImage);

		fileChooser.setTitle("Upload Image");
		File filePath = fileChooser.showOpenDialog(null);

		if (filePath != null) {
			String fileNameWithExtension = filePath.toString().substring(
					filePath.toString().lastIndexOf("/") + 1);
			String fileNameWithoutExtension = fileNameWithExtension.substring(
					0, fileNameWithExtension.lastIndexOf("."));

			metaData.setTitle(fileNameWithoutExtension);
			metaData.setFilename(fileNameWithExtension);

			/*
			 * if (config file == flickr) uploadImageToFlickr(); else if (config
			 * file == imgur) uploadImageToImgur(); else throw error?
			 */
			// addImage(filePath); //file = the entire file path
			uploadImageToFlickr(filePath);
		}
	}

	// By default
	private File uploadImageToFlickr(File filePath) {
		if (optionArgs.size() > 0 || optionArgs != null) {
			for (int i = 0; i < optionArgs.size(); i++) {
				uploadToFlickrUsingFlickr4Java.addOption(optionArgs.get(i));
			}
		}

		if (!uploadToFlickrUsingFlickr4Java.canUpload()) {
			// print error pop up
		}

		try {
			uploadToFlickrUsingFlickr4Java.getPrivacy();
			uploadToFlickrUsingFlickr4Java.getPhotosetsInfo();
			if (metaData.getFilename() != null
					&& !metaData.getFilename().equals("")) {
				uploadToFlickrUsingFlickr4Java.getSetPhotos(metaData
						.getFilename());
			}
			String photoId = uploadToFlickrUsingFlickr4Java.processFileArg(
					filePath, metaData.getFilename());
			System.out.println("photoId: " + photoId);
			if (!photoId.isEmpty() && photoId != null && !photoId.equals("")) {
				Photo photoInfo = uploadToFlickrUsingFlickr4Java.getPhotoURL(
						photoId, "");
				if (photoInfo != null) {
					addImageToCaretPosition(photoInfo.getOriginalUrl());
				} else {
					System.out.println("PhotoInfo is null");
				}
			} else {
				System.out.println("Please upload an image file");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}