import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import DualListBox from 'react-dual-listbox';
import { toast } from 'react-toastify';

import 'react-dual-listbox/lib/react-dual-listbox.css';

import Select from '../utils/select';
import DatePicker from '../utils/date-picker';
import { resetLogoutCounter } from '../actions/index';
import apiClient from '../utils/api-client';
import { hasAuthority, ASSIGN_MODULES_AUTHORITY } from '../utils/authorization';

class AssignModules extends Component {
  constructor(props) {
    super(props);
    this.state = {
      availableModulesList: [],
      selectedModules: [],
      availableChws: [],
      selectedChw: this.props.match.params.chwId || '',
      currentModules: [],
      delayNotification: false,
      notificationTime: '',
    };

    this.handleModuleChange = this.handleModuleChange.bind(this);
    this.fetchAvailableModules = this.fetchAvailableModules.bind(this);
    this.sendAssignedModules = this.sendAssignedModules.bind(this);
    this.fetchChwModules = this.fetchChwModules.bind(this);
    this.areModulesEqual = this.areModulesEqual.bind(this);
    this.handleNotificationTimeChange = this.handleNotificationTimeChange.bind(this);
  }

  componentDidMount() {
    if (!hasAuthority(ASSIGN_MODULES_AUTHORITY)) {
      this.props.history.push('/home');
    } else {
      this.fetchAvailableModules();
      this.fetchChwsInfo();
    }
  }

  fetchChwsInfo = () => {
    const url = 'api/chwInfo';

    apiClient.get(url)
      .then((response) => {
        const availableChws = _.map(
          response.data,
          chw => ({ value: chw.id, label: `${chw.chwId}: ${_.defaultTo(chw.chwName, '')}` }),
        );

        this.setState({ availableChws });
      });
  };

  handleChwChange = (selectedChw) => {
    if (selectedChw) {
      this.setState({ selectedChw }, () => this.fetchChwModules());
    } else {
      this.setState({ selectedChw: '' });
    }
  };

  handleModuleChange(selectedModules) {
    this.setState({ selectedModules });
  }

  handleNotificationTimeChange(notificationTime) {
    this.setState({ notificationTime });
    this.props.resetLogoutCounter();
  }

  fetchAvailableModules() {
    const url = '/api/modules/simple';

    apiClient.get(url)
      .then((response) => {
        const availableModulesList = _.map(
          response.data,
          module => ({ value: module.id, label: module.name }),
        );
        this.setState({ availableModulesList });
        if (this.state.selectedChw) {
          this.fetchChwModules();
        }
      });
  }

  fetchChwModules() {
    const url = '/api/assignedModules';
    const params = {
      chwId: this.state.selectedChw,
    };

    apiClient.get(url, { params })
      .then((response) => {
        const selectedModules = _.map(
          response.data.modules,
          module => module.id,
        );
        this.setState({
          selectedModules,
          currentModules: selectedModules,
        });
      });
  }

  sendAssignedModules() {
    const url = '/api/module/assign';

    const payload = {
      modules: this.state.selectedModules,
      chwId: this.state.selectedChw,
    };
    if (this.state.delayNotification && this.state.notificationTime) {
      payload.notificationTime = this.state.notificationTime;
    }
    const callback = () => {
      this.props.history.push('/chw/selected');
      toast.success('Modules have been assigned!');
    };

    apiClient.post(url, payload)
      .then(() => callback());
  }

  areModulesEqual() {
    return _.isEqual(this.state.selectedModules.sort(), this.state.currentModules.sort());
  }

  render() {
    const { selectedModules, notificationTime } = this.state;
    const { availableModulesList } = this.state;

    return (
      <div>
        <h1 className="page-header padding-bottom-xs margin-x-sm text-center">Assign Modules</h1>
        <div className="col-md-8 offset-md-2">
          <Select
            name="form-field-name"
            value={this.state.selectedChw}
            options={this.state.availableChws}
            onChange={this.handleChwChange}
            onFocus={() => this.props.resetLogoutCounter()}
            placeholder="Select a Community Health Worker"
            className="margin-bottom-md"
          />
          <DualListBox
            canFilter
            options={availableModulesList}
            selected={selectedModules}
            filterPlaceholder="Filter..."
            onChange={this.handleModuleChange}
            onFocus={() => this.props.resetLogoutCounter()}
            disabled={this.state.selectedChw === ''}
          />
          <div className="col-md-12 margin-top-sm margin-bottom-xs">
            <input
              id="delay-notification"
              type="checkbox"
              checked={this.state.delayNotification}
              onChange={event => this.setState({ delayNotification: event.target.checked })}
            />
            {/* eslint-disable-next-line jsx-a11y/label-has-associated-control */}
            <label htmlFor="delay-notification" className="margin-left-sm margin-bottom-sm">
              Delay the notification
            </label>
          </div>
          {this.state.delayNotification
          && (
          <div className="col-md-12 margin-top-sm">
            {/* eslint-disable-next-line jsx-a11y/label-has-associated-control */}
            <label htmlFor="notification-time">Notification date</label>
            <DatePicker
              id="notification-time"
              showTimeSelect
              value={notificationTime}
              onChange={this.handleNotificationTimeChange}
              minDate={new Date()}
            />
          </div>
          )}
          <form
            className="col-md-12"
            onSubmit={this.sendAssignedModules}
          >
            <button
              type="submit"
              className="btn btn-primary btn-block
                      margin-x-md padding-x-sm"
              style={{ marginBottom: '200px' }}
              disabled={this.areModulesEqual()}
            >
              <h4>Assign!</h4>
            </button>
          </form>
        </div>
      </div>
    );
  }
}

export default connect(null, { resetLogoutCounter })(AssignModules);

AssignModules.propTypes = {
  match: PropTypes.shape({
    params: PropTypes.shape({
      chwId: PropTypes.string,
    }),
  }).isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
  resetLogoutCounter: PropTypes.func.isRequired,
};
