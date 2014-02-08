/**
 * This packages holds every type of calculation that need to be computed by
 * the application (gisement, station orientation, ...).
 * 
 * <p>Here is the procedure for creating a new calculation:</p>
 * <ul>
 *      <li>
 *          Create a new class that extends the abstract class
 *          {@link ch.hgdev.toposuite.calculation.Calculation}
 *      </li>
 *      <li>
 *          Since {@link ch.hgdev.toposuite.calculation.Calculation} implements the
 *          {@link ch.hgdev.toposuite.calculation.interfaces.Exportable}, Importable and
 *          DAOUpdate interfaces, you have to Override the following methods:
 *          <ul>
 *              <li>
 *                  <strong>{@link ch.hgdev.toposuite.calculation.interfaces.Exportable#exportToJSON()}:</strong>
 *                  This method must return a JSON string that contains the input arguments
 *                  of the calculations. For instance, the Gisement takes 2 points
 *                  ({@link ch.hgdev.toposuite.points.Point}), so the exported JSON will
 *                  contain the numbers of these points. 
 *              </li>
 *              <li>
 *                  <strong>{@link ch.hgdev.toposuite.calculation.interfaces.Importable#importFromJSON(String)}:</strong>
 *                  This method is the inverse of the previous one. It receives a JSON string
 *                  as argument and must unserialize it in order to set the corresponding
 *                  attributes.
 *              </li>
 *              <li>
 *                  <strong>{@link ch.hgdev.toposuite.calculation.Calculation#getActivityClass()}:</strong>
 *                  This method should return the class of the activity corresponding to the
 *                  calculation. For instance,
 *                  {@link ch.hgdev.toposuite.calculation.Gisement#getActivityClass()}
 *                  returns <em>GisementActivity.class</em>.
 *              </li>
 *          </ul>
 *      </li>
 *      <li>Add a new field for your calculation in {@link CalculationType}.</li>
 *      <li>
 *          You also have to edit the {@link ch.hgdev.toposuite.calculation.CalculationFactory}.
 *          In the switch_case statement of
 *          {@link CalculationFactory#createCalculation(CalculationType, long, String, java.util.Date, String)},
 *          you must add a case for the enum field of your new
 *          {@link ch.hgdev.toposuite.calculation.Calculation}.
 *      </li>
 *      <li>
 *          Don't forget to make appropriate constructors. See
 *          {@link ch.hgdev.toposuite.calculation.Gisement}
 *      </li>
 * </ul>
 * 
 * @author HGdev

 */
package ch.hgdev.toposuite.calculation;