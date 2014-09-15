package org.jibx.schema.test.company;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.sampleschema.address.Address;
import org.jibx.sampleschema.company.Company;
import org.jibx.sampleschema.person.Person;
import org.junit.Assert;
import org.junit.Test;

public class CompanyTest {

    @Test
    public void test() throws Exception {
        Company testObject = createTestObject();
        String xml = marshalObject(testObject);
        System.out.println(xml);
        Company actualObject = (Company) unmarshalObject(xml);
        checkObject(actualObject);
    }

    private Company createTestObject() {
        Company company = new Company();
        company.setCompanyName("My Company Name");
        Address address = new Address();
        company.setAddress(address);
        address.setStreet("My Street");
        address.setCity("My City");
        Person person = new Person();
        address.setPerson(person);
        person.setFirstName("My First Name");
        person.setLastName("My Last Name");
        return company;
    }

    private final static String STRING_ENCODING = "UTF8";

    private final static String URL_ENCODING = "UTF-8";

    private String marshalObject(Company testObject) throws Exception {
        IBindingFactory jc = BindingDirectory.getFactory(Company.class);
        IMarshallingContext marshaller = jc.createMarshallingContext();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshalDocument(testObject, URL_ENCODING, null, out);
        String xml = out.toString(STRING_ENCODING);
        return xml;
    }

    private final static String BINDING_NAME = "binding";

    public Object unmarshalObject(String xml) throws Exception {
        IBindingFactory jc = BindingDirectory.getFactory(Company.class);
        IUnmarshallingContext unmarshaller = jc.createUnmarshallingContext();
        Reader inStream = new StringReader(xml);
        Object object = unmarshaller.unmarshalDocument(inStream, BINDING_NAME);
        return object;
    }

    private void checkObject(Company company) {
        Assert.assertEquals("My Company Name", company.getCompanyName());
        Assert.assertEquals("My Street", company.getAddress().getStreet());
        Assert.assertEquals("My City", company.getAddress().getCity());
        Assert.assertEquals("My First Name", company.getAddress().getPerson().getFirstName());
        Assert.assertEquals("My Last Name", company.getAddress().getPerson().getLastName());
    }

}