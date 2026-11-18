import { useEffect } from "react";
import { useState } from "react";
import { signUp } from "../../services/user-service";
import { toast } from "react-toastify";
import { loadAllDepartments } from "../../services/department-service";
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Col,
  Container,
  Form,
  FormFeedback,
  FormGroup,
  Input,
  Label,
  Row,
} from "reactstrap";
const Signup = () => {
  const [departments, setDepartments] = useState([]);
  const [data, setData] = useState({
    name: "",
    email: "",
    password: "",
    about: "",
    depId: "",
  });

  const [error, setError] = useState({
    errors: {},
    isError: false,
  });

  useEffect(() => {
    loadAllDepartments()
      .then((data) => {
        setDepartments(data);
      })
      .catch(() => {
        toast.error("Failed to load departments");
      });
  }, []);

  const handleChange = (event, property) => {
    setData({ ...data, [property]: event.target.value });
  };

  const resetData = () => {
    setData({
      name: "",
      email: "",
      password: "",
      about: "",
      depId: "",
    });
  };

  const submitForm = (event) => {
    event.preventDefault();

    signUp(data)
      .then((resp) => {
        toast.success(
          "User is registered successfully with user id " + resp.id
        );
        setData({
          name: "",
          email: "",
          password: "",
          about: "",
          depId: "",
        });
      })
      .catch((error) => {
        toast.error("Failed to register");
        setError({
          errors: error,
          isError: true,
        });
      });
  };

  return (
    <Container>
      <Row className="mt-4">
        <Col sm={{ size: 6, offset: 3 }}>
          <Card
            style={{
              backgroundColor: "#f2f2f2",
            }}
          >
            <CardHeader>
              <h3>Gebruiker registreren</h3>
            </CardHeader>

            <CardBody>
              <Form onSubmit={submitForm}>
                <FormGroup>
                  <Label for="name">Naam</Label>
                  <Input
                    type="text"
                    placeholder="Enter here"
                    id="name"
                    required
                    onChange={(e) => handleChange(e, "name")}
                    value={data.name}
                    invalid={error.errors?.response?.data?.name ? true : false}
                  />

                  <FormFeedback>
                    {error.errors?.response?.data?.name}
                  </FormFeedback>
                </FormGroup>

                <FormGroup>
                  <Label for="email">E-mailadres</Label>
                  <Input
                    type="email"
                    placeholder="Enter here"
                    id="email"
                    required
                    onChange={(e) => handleChange(e, "email")}
                    value={data.email}
                    invalid={error.errors?.response?.data?.email ? true : false}
                  />

                  <FormFeedback>
                    {error.errors?.response?.data?.email}
                  </FormFeedback>
                </FormGroup>

                <FormGroup>
                  <Label for="password">Wachtwoord</Label>
                  <Input
                    type="password"
                    placeholder="Enter here"
                    id="password"
                    minLength={8}
                    maxLength={72}
                    required
                    onChange={(e) => handleChange(e, "password")}
                    value={data.password}
                    invalid={
                      error.errors?.response?.data?.password ? true : false
                    }
                  />

                  <FormFeedback>
                    {error.errors?.response?.data?.password}
                  </FormFeedback>
                </FormGroup>

                <FormGroup>
                  <Label for="about">Opmerkingen</Label>
                  <Input
                    type="textarea"
                    placeholder="Enter here"
                    id="about"
                    style={{ height: "100px" }}
                    onChange={(e) => handleChange(e, "about")}
                    value={data.about}
                    invalid={error.errors?.response?.data?.about ? true : false}
                  />

                  <FormFeedback>
                    {error.errors?.response?.data?.about}
                  </FormFeedback>
                </FormGroup>

                <FormGroup>
                  <Label for="depId">Afdeling</Label>
                  <Input
                    type="select"
                    id="depId"
                    placeholder="Select here"
                    className="rounded-0"
                    name="depId"
                    onChange={(event) => handleChange(event, "depId")}
                    defaultValue={0}
                    required
                  >
                    <option disabled value={0}>
                      --Afdeling Selecteren--
                    </option>

                    {departments.map((department) => (
                      <option value={department.id} key={department.id}>
                        {department.depName === "ADMIN"
                          ? "Admin"
                          : department.depName === "Logistiek"
                          ? "Logistiek"
                          : department.depName === "Verkoop"
                          ? "Verkoop"
                          : department.depName === "Werkplaats"
                          ? "Werkplaats"
                          : department.depName === "Smederij"
                          ? "Smederij"
                          : "N/A"}
                      </option>
                    ))}
                  </Input>
                  <FormFeedback>
                    {error.errors?.response?.data?.departments}
                  </FormFeedback>
                </FormGroup>

                <Container className="text-center">
                  <Button outline color="dark">
                    registreren
                  </Button>
                  <Button
                    onClick={resetData}
                    color="secondary"
                    type="reset"
                    className="ms-2"
                  >
                    reset
                  </Button>
                </Container>
              </Form>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Signup;
