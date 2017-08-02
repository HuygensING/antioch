package nl.knaw.huygens.alexandria.endpoint.command;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import nl.knaw.huygens.alexandria.endpoint.command.XpathCommand.XPathResult;
import nl.knaw.huygens.alexandria.endpoint.command.XpathCommand.XPathResult.Type;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class XpathCommandTest extends AlexandriaTest {
  String EXAMPLE = singleQuotesToDouble("<root xmlns:bar='http://www.bar.org' xmlns:foo='http://www.foo.org/'>"//
      + "<employees>"//
      + "<employee id='1'>Johnny Dapp</employee>"//
      + "<employee id='2'>Al Pacino</employee>"//
      + "<employee id='3'>Robert De Niro</employee>"//
      + "<employee id='4'>Kevin Spacey</employee>"//
      + "<employee id='5'>Denzel Washington</employee>"//
      + "</employees>"//
      + "<foo:companies>"//
      + "<foo:company id='6'>Tata Consultancy Services</foo:company>"//
      + "<foo:company id='7'>Wipro</foo:company>"//
      + "<foo:company id='8'>Infosys</foo:company>"//
      + "<foo:company id='9'>Microsoft</foo:company>"//
      + "<foo:company id='10'>IBM</foo:company>"//
      + "<foo:company id='11'>Apple</foo:company>"//
      + "<foo:company id='12'>Oracle</foo:company>"//
      + "</foo:companies>"//
      + "</root>");

  @Test
  public void selectRootElement() throws Exception {
    XPathResult result = XpathCommand.testXPath("/root", EXAMPLE);
    assertThat(result.getType()).isEqualTo(Type.NODESET);
    assertThat(result.getResult()).isInstanceOf(List.class);
    assertThat((List<String>) result.getResult()).containsExactly(EXAMPLE);
  }

  @Test
  public void selectAllEmployeeElementsThatAreDirectChildrenOfTheEmployeesElement() throws Exception {
    XPathResult result = XpathCommand.testXPath("/root/employees/employee", EXAMPLE);
    softly.assertThat(result.getType()).isEqualTo(Type.NODESET);
    softly.assertThat(result.getResult()).isInstanceOf(List.class);
    softly.assertThat((List<String>) result.getResult()).containsExactly(//
        "<employee id=\"1\">Johnny Dapp</employee>", //
        "<employee id=\"2\">Al Pacino</employee>", //
        "<employee id=\"3\">Robert De Niro</employee>", //
        "<employee id=\"4\">Kevin Spacey</employee>", //
        "<employee id=\"5\">Denzel Washington</employee>"//
    );
  }

  @Test
  public void selectTheTextualValueOfTheFirstEmployeeElement() throws Exception {
    XPathResult result = XpathCommand.testXPath("string(//employee[1])", EXAMPLE);
    softly.assertThat(result.getType()).isEqualTo(Type.STRING);
    softly.assertThat(result.getResult()).isEqualTo("Johnny Dapp");
  }

  @Test
  public void selectTheTextualValueOfTheSecondEmployeeElement() throws Exception {
    XPathResult result = XpathCommand.testXPath("//employee[2]/text()", EXAMPLE);
    softly.assertThat(result.getType()).isEqualTo(Type.NODESET);
    softly.assertThat(result.getResult()).isEqualTo(Collections.singletonList("Al Pacino"));
  }

  @Test
  public void selectTheNumberOfEmployeeElements() throws Exception {
    XPathResult result = XpathCommand.testXPath("count(//employee)", EXAMPLE);
    softly.assertThat(result.getType()).isEqualTo(Type.NUMBER);
    softly.assertThat(result.getResult()).isEqualTo(5.0);
  }

  @Test
  public void selectTheNumberOfCompanyElements() throws Exception {
    XPathResult result = XpathCommand.testXPath("count(//foo:company)", EXAMPLE);
    softly.assertThat(result.getType()).isEqualTo(Type.NUMBER);
    softly.assertThat(result.getResult()).isEqualTo(7.0);
  }

  @Test
  public void selectTheLocalNameOfTheFirstCompanyElement() throws Exception {
    XPathResult result = XpathCommand.testXPath("local-name(//foo:company[1])", EXAMPLE);
    softly.assertThat(result.getType()).isEqualTo(Type.STRING);
    softly.assertThat(result.getResult()).isEqualTo("company");
  }

  @Test
  public void selectTrue() throws Exception {
    XPathResult result = XpathCommand.testXPath("true()", EXAMPLE);
    softly.assertThat(result.getType()).isEqualTo(Type.BOOLEAN);
    softly.assertThat(result.getResult()).isEqualTo(true);
  }

  @Test
  public void selectFalse() throws Exception {
    XPathResult result = XpathCommand.testXPath("false()", EXAMPLE);
    softly.assertThat(result.getType()).isEqualTo(Type.BOOLEAN);
    softly.assertThat(result.getResult()).isEqualTo(false);
  }

  @Test
  public void floor() throws Exception {
    XPathResult result = XpathCommand.testXPath("floor(3.1415926)", EXAMPLE);
    softly.assertThat(result.getType()).isEqualTo(Type.NUMBER);
    softly.assertThat(result.getResult()).isEqualTo(3.0);
  }

  @Test
  public void ceiling() throws Exception {
    XPathResult result = XpathCommand.testXPath("ceiling(3.1415926)", EXAMPLE);
    softly.assertThat(result.getType()).isEqualTo(Type.NUMBER);
    softly.assertThat(result.getResult()).isEqualTo(4.0);
  }

}
