//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.02 at 04:53:08 PM BST 
//


package movies;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the movies package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Star_QNAME = new QName("", "Star");
    private final static QName _Genre_QNAME = new QName("", "Genre");
    private final static QName _Score_QNAME = new QName("", "Score");
    private final static QName _Director_QNAME = new QName("", "Director");
    private final static QName _Title_QNAME = new QName("", "Title");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: movies
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link StarList }
     * 
     */
    public StarList createStarList() {
        return new StarList();
    }

    /**
     * Create an instance of {@link DirectorList }
     * 
     */
    public DirectorList createDirectorList() {
        return new DirectorList();
    }

    /**
     * Create an instance of {@link Movie }
     * 
     */
    public Movie createMovie() {
        return new Movie();
    }

    /**
     * Create an instance of {@link MovieCatalog }
     * 
     */
    public MovieCatalog createMovieCatalog() {
        return new MovieCatalog();
    }

    /**
     * Create an instance of {@link GenresList }
     * 
     */
    public GenresList createGenresList() {
        return new GenresList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Star")
    public JAXBElement<String> createStar(String value) {
        return new JAXBElement<String>(_Star_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Genre")
    public JAXBElement<String> createGenre(String value) {
        return new JAXBElement<String>(_Genre_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Score")
    public JAXBElement<BigInteger> createScore(BigInteger value) {
        return new JAXBElement<BigInteger>(_Score_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Director")
    public JAXBElement<String> createDirector(String value) {
        return new JAXBElement<String>(_Director_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Title")
    public JAXBElement<String> createTitle(String value) {
        return new JAXBElement<String>(_Title_QNAME, String.class, null, value);
    }

}
