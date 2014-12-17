package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

public class XMLHelper {
	
	/**
	 * Unmarshals a given String with XML content to a XML java object.
	 * 
	 * @param docClass - Class of the XML java object to be created
	 * @param source - String with XML content
	 * @return new XML java Object created
	 * @throws JAXBException
	 */
	public static <T> Object unmarshal( Class <T> docClass, String source) throws JAXBException  {
		return unmarshal(docClass, new StringReader(source) );
	}
	
	/**
	 * Unmarshals a given File with XML content to a XML java object.
	 * 
	 * @param docClass - Class of the XML java object to be created
	 * @param source - File with XML content
	 * @return new XML java Object created
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	public static <T> Object unmarshal( Class <T> docClass, File source) throws JAXBException, FileNotFoundException {
		return unmarshal(docClass, new FileReader(source) );
	}
	
	private static <T> Object unmarshal( Class<T> docClass, Reader reader ) throws JAXBException {
		String packageName = docClass.getPackage().getName();
		
		JAXBContext jaxbcontext = JAXBContext.newInstance( packageName );
		Unmarshaller um = jaxbcontext.createUnmarshaller();
		
		Object o = um.unmarshal(reader);
		
		return o;
	}
	
	/**
	 * Marshals a given XML java object to a given File
	 * 
	 * @param o - Object to be marshaled
	 * @param target - File to output
	 * @param formatedOutput - Option to format the output or not, false by default
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static <T> void marshal( T o , File target, Boolean formatedOutput) throws JAXBException, IOException {
		FileWriter fw = new FileWriter(target);
		try {
			marshal(o, fw, null, formatedOutput);
		} catch (SAXException e) {
			// no validation file was given so we ignore this exception 
		}
	}
	
	/**
	 * Marshals a given XML java object to String.
	 * 
	 * @param o - Object to be marshaled
	 * @param formatedOutput - Option to format the output or not, false by default
	 * @return String with the marshal of the XML Java Object
	 * @throws JAXBException
	 */
	public static <T> String marshal( T o , Boolean formatedOutput) throws JAXBException {
		StringWriter sw = new StringWriter();
		String output;
		
		try {
			marshal(o, sw, null, formatedOutput);
		} catch (SAXException e) {
			// no validation file was given so we ignore this exception 
		} catch (FileNotFoundException e) {
			// ignore, no XSD file was given to the method
		}
		output = sw.toString();
		try {
			sw.close();
		} catch (IOException e) {
			/*ignore*/
		}
		return output;
	}
	
	/**
	 * Marshals a given XML java object to a given File
	 * The content is verified against the given schema.
	 * 
	 * @param o - Object to be marshaled
	 * @param target - File to output
	 * @param xsdFile - Name of the XSD file
	 * @param formatedOutput - Option to format the output or not, false by default
	 * @throws JAXBException
	 * @throws IOException - If the xsdFile does not exists
	 * @throws SAXException - If the XSD verification fails
	 */
	public static <T> void marshal( T o, File target, String xsdFile, Boolean formatedOutput) throws JAXBException, IOException, SAXException {
		FileWriter fw = new FileWriter(target);
		marshal(o, fw, xsdFile, formatedOutput);
	}
	
	/**
	 * Marshals a given XML java object to String.
	 * The content is verified against the given schema.
	 * 
	 * @param o - Object to be marshaled
	 * @param xsdFile - Name of the XSD file
	 * @param formatedOutput - Option to format the output or not, false by default
	 * @return String with the marshal of the XML Java Object
	 * @throws JAXBException 
	 * @throws IOException - If the xsdFile does not exists
	 * @throws SAXException - If the XSD verification fails
	 */
	public static <T> String marshal( T o, String xsdFile, Boolean formatedOutput) throws JAXBException, IOException, SAXException {
		StringWriter sw = new StringWriter();
		String output;
		
		marshal(o, sw, xsdFile, formatedOutput);
		
		output = sw.toString();
		try {
			sw.close();
		} catch (IOException e) {
			/*ignore*/
		}
		return output;
	}
	
	private static <T> void marshal( T o , Writer target, String xsdFile, Boolean formatedOutput) throws FileNotFoundException, JAXBException, SAXException {
		String packageName = o.getClass().getPackage().getName();
		
		// SCHEMA VERIFICATION
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema s = null;
		if (xsdFile != null) {
			File xsdf = new File(xsdFile);
			if (!xsdf.exists()) throw new FileNotFoundException();
			s = sf.newSchema(xsdf);
		}
		
		JAXBContext jaxbContext = JAXBContext.newInstance( packageName );
		Marshaller m = jaxbContext.createMarshaller();
		m.setSchema(s);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, (formatedOutput == null) ? false : formatedOutput); // for getting nice formatted output

		m.marshal(o, target);
	}

}
